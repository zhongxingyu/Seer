 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.core.storage.genericSQL;
 
 import com.flexive.core.storage.FxTreeNodeInfo;
 import com.flexive.core.storage.FxTreeNodeInfoSpreaded;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.content.FxPermissionUtils;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.exceptions.FxTreeException;
 import com.flexive.shared.exceptions.FxUpdateException;
 import com.flexive.shared.interfaces.ContentEngine;
 import com.flexive.shared.interfaces.SequencerEngine;
 import com.flexive.shared.structure.FxType;
 import com.flexive.shared.tree.FxTreeMode;
 import com.flexive.shared.tree.FxTreeNode;
 import com.flexive.shared.value.FxString;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.sql.*;
 import java.util.Stack;
 
 /**
  * Generic tree storage implementation using a spreaded nested set tree
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @author Gregor Schober (gregor.schober@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class GenericTreeStorageSpreaded extends GenericTreeStorage {
     private static final transient Log LOG = LogFactory.getLog(GenericTreeStorageSpreaded.class);
 
     protected static final BigDecimal TWO = new BigDecimal(2);
     protected static final BigDecimal THREE = new BigDecimal(3);
     protected static final BigDecimal GO_UP = new BigDecimal(1024);
     protected static final BigDecimal MAX_RIGHT = new BigDecimal("18446744073709551615");
 //    protected static final BigDecimal MAX_RIGHT = new BigDecimal("1000");
 //    protected static final BigDecimal GO_UP = new BigDecimal(10);
 
     private static final String TREE_LIVE_MAXRIGHT = "SELECT MAX(RGT) FROM " + getTable(FxTreeMode.Live) +
             //            1
             " WHERE PARENT=?";
     private static final String TREE_EDIT_MAXRIGHT = "SELECT MAX(RGT) FROM " + getTable(FxTreeMode.Edit) +
             //            1
             " WHERE PARENT=?";
 
 
     /**
      * {@inheritDoc}
      */
     public FxTreeNodeInfo getTreeNodeInfo(Connection con, FxTreeMode mode, long nodeId) throws FxApplicationException {
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement(mode == FxTreeMode.Live ? TREE_LIVE_MAXRIGHT : TREE_EDIT_MAXRIGHT);
             ps.setLong(1, nodeId);
             ResultSet rs = ps.executeQuery();
             if (rs == null || !rs.next())
                 throw new FxNotFoundException("ex.tree.node.notFound", nodeId, mode);
             BigDecimal maxRight = rs.getBigDecimal(1);
             ps.close();
             ps = con.prepareStatement(mode == FxTreeMode.Live ? TREE_LIVE_NODEINFO : TREE_EDIT_NODEINFO);
             ps.setLong(1, nodeId);
             rs = ps.executeQuery();
             if (rs == null || !rs.next())
                 throw new FxNotFoundException("ex.tree.node.notFound", nodeId, mode);
             long _acl = rs.getLong(14);
             FxType _type = CacheAdmin.getEnvironment().getType(rs.getLong(15));
             long _stepACL = CacheAdmin.getEnvironment().getStep(rs.getLong(17)).getAclId();
             long _createdBy = rs.getLong(18);
             long _mandator = rs.getLong(19);
             boolean[] perms = FxPermissionUtils.getPermissions(_acl, _type, _stepACL, _createdBy, _mandator);
             return new FxTreeNodeInfoSpreaded(rs.getBigDecimal(1), rs.getBigDecimal(2), rs.getBigDecimal(5),
                     rs.getBigDecimal(6), maxRight, rs.getInt(4), rs.getInt(8), rs.getInt(7), rs.getLong(3),
                     nodeId, rs.getString(12), new FxPK(rs.getLong(9), rs.getInt(16)),
                     _acl, mode, rs.getInt(13), rs.getString(10), rs.getLong(11),
                     perms[0], perms[4], perms[2], perms[1], perms[3]);
         } catch (SQLException e) {
             throw new FxTreeException(e, "ex.tree.nodeInfo.sqlError", nodeId, e.getMessage());
         } finally {
             try {
                 if (ps != null)
                     ps.close();
             } catch (SQLException e) {
                 LOG.error(e, e);
             }
         }
     }
 
     /**
      * Calculate the boundaries for a new position.
      *
      * @param con      an open and valid connection
      * @param node     node to operate on
      * @param position the new position to get the boundaries for
      * @return the left and right boundary
      * @throws com.flexive.shared.exceptions.FxTreeException
      *          if the function fails
      */
     public BigDecimal[] getBoundaries(Connection con, FxTreeNodeInfoSpreaded node, int position) throws FxApplicationException {
         // Position cleanup
         if (position < 0)
             position = 0;
 
         // any childs at all? If not we just return the node boundaries
         if (!node.hasChildren())
             return new BigDecimal[]{node.getLeft(), node.getRight()};
 
         // Max position?
         if (position >= node.getDirectChildCount())
             return new BigDecimal[]{node.getMaxChildRight(), node.getRight()};
 
         Statement stmt = null;
         // Somewhere between the child nodes
         try {
             BigDecimal leftBoundary;
             BigDecimal rightBoundary;
 
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM (" +
                     "SELECT LFT,RGT FROM " + getTable(node.getMode()) + " WHERE PARENT=" + node.getId() +
                     " ORDER BY LFT ASC) SUB " +
                     " LIMIT " + ((position == 0) ? 0 : position - 1) + ",2");
             if (rs.next()) {
                 if (position == 0) {
                     /* first position */
                     leftBoundary = node.getLeft();
                     rightBoundary = rs.getBigDecimal(1);
                 } else {
                     /* somewhere between 2 children or after last child */
                     leftBoundary = rs.getBigDecimal(2);
 //                    rightBoundary = rs.getBigDecimal(1);
                     if (rs.next())
                         rightBoundary = rs.getBigDecimal(1);
                     else
                         rightBoundary = node.getRight();
                 }
             } else {
                 throw new FxTreeException("ex.tree.boundaries.computeFailed", node.getId(), position, "Invalid position [" + position + "] to calculate boundaries!");
             }
             return new BigDecimal[]{leftBoundary, rightBoundary};
         } catch (Exception e) {
             throw new FxTreeException(e, "ex.tree.boundaries.computeFailed", node.getId(), position, e.getMessage());
         } finally {
             try {
                 if (stmt != null) stmt.close();
             } catch (Throwable t) {
                 /*ignore*/
             }
         }
     }
 
     /**
      * Creates space for an additional amount of nodes at the specified position in the specified node.
      *
      * @param con        an open and valid connection
      * @param seq        reference to the sequencer
      * @param mode       tree mode
      * @param nodeId     the node to work on
      * @param position   the position within the child nodes (0 based)
      * @param additional the amount of additional nodes to make space for
      * @return the used spacing
      * @throws FxApplicationException on errors
      */
     public BigDecimal makeSpace(Connection con, SequencerEngine seq, FxTreeMode mode, long nodeId, int position, final int additional) throws FxApplicationException {
 
         FxTreeNodeInfoSpreaded nodeInfo = (FxTreeNodeInfoSpreaded) getTreeNodeInfo(con, mode, nodeId);
         BigDecimal boundaries[] = getBoundaries(con, nodeInfo, position);
 
         int totalChildCount = nodeInfo.getTotalChildCount() + additional;
         boolean hasSpace = nodeInfo.hasSpaceFor(totalChildCount, 2);
         /*if( hasSpace )
             return nodeInfo.getSpacing(totalChildCount);*/
 
         // Determine node to work on
         while (!hasSpace) {
             nodeInfo = (FxTreeNodeInfoSpreaded) getTreeNodeInfo(con, mode, nodeInfo.getParentId());
             totalChildCount += nodeInfo.getTotalChildCount() + 1;
             hasSpace = nodeInfo.hasSpaceFor(totalChildCount, 2);
             if (!hasSpace && nodeInfo.isRoot()) {
                 throw new FxUpdateException("ex.tree.makeSpace.failed");
             }
         }
 
         // Allocate/Reorganize space
         BigDecimal spacing = nodeInfo.getSpacing(totalChildCount);
         int spaceCount = (additional * 2) + 1;
         BigDecimal insertSpace = spacing.multiply(new BigDecimal(spaceCount));
         insertSpace = insertSpace.add(new BigDecimal(additional * 2));
 
         reorganizeSpace(con, seq, mode, mode, nodeInfo.getId(), false, spacing, null, nodeInfo, position,
                 insertSpace, boundaries, 0, null, false, false, null);
         return spacing;
     }
 
     /**
      * Do what i mean function :-D
      *
      * @param con                an open and valid Connection
      * @param seq                a valid Sequencer reference
      * @param sourceMode         the source table (matters in createMode only)
      * @param destMode           the destination table (matters in createMode only)
      * @param nodeId             the node to work on
      * @param includeNodeId      if true the operations root node (nodeId) is included into the updates
      * @param overrideSpacing    if set this spacing is used instead of the computed one
      * @param overrideLeft       if set this will be the first left position
      * @param insertParent       create mode only: the parent node in which we will generate the free space
      *                           specified by the parameters [insertPosition] and [insertSpace]
      * @param insertPosition     create mode only: the position withn the destination nodes childs
      * @param insertSpace        create mode only: the space to keep free at the specified position
      * @param insertBoundaries   create mode only: the insert boundaries
      * @param depthDelta         create mode only: the delta to apply to the depth
      * @param destinationNode    create mode only: the destination node
      * @param createMode         if true the function will insert copy of nodes instead of updating them
      * @param createKeepIds      keep the ids in create mode
      * @param firstCreatedNodeId first created node id
      * @return true if the function was successfully
      * @throws FxTreeException if the function fails
      */
     public boolean reorganizeSpace(Connection con, SequencerEngine seq,
                                    FxTreeMode sourceMode, FxTreeMode destMode,
                                    long nodeId, boolean includeNodeId, BigDecimal overrideSpacing, BigDecimal overrideLeft,
                                    FxTreeNodeInfo insertParent, int insertPosition, BigDecimal insertSpace, BigDecimal insertBoundaries[],
                                    int depthDelta, Long destinationNode, boolean createMode, boolean createKeepIds, Long firstCreatedNodeId) throws FxTreeException {
         FxTreeNodeInfoSpreaded nodeInfo;
         try {
             nodeInfo = (FxTreeNodeInfoSpreaded) getTreeNodeInfo(con, sourceMode, nodeId);
 //                    new FxNodeInfo(con, sourceTbl, nodeId);
         } catch (Exception e) {
             return false;
         }
 
         if (!nodeInfo.isSpaceOptimizable()) {
             // The Root node and cant be optimize any more ... so all we can do is fail :-/
             // This should never really happen
             if (nodeId == ROOT_NODE) {
                 return false;
             }
             //System.out.println("### UP we go");
             return reorganizeSpace(con, seq, sourceMode, destMode, nodeInfo.getParentId(), includeNodeId, overrideSpacing, overrideLeft, insertParent,
                     insertPosition, insertSpace, insertBoundaries, depthDelta, destinationNode, createMode, createKeepIds, firstCreatedNodeId);
         }
 
         BigDecimal SPACING = nodeInfo.getDefaultSpacing();
         if (overrideSpacing != null && overrideSpacing.compareTo(SPACING) < 0) {
             SPACING = overrideSpacing;
         } else {
             if (SPACING.compareTo(GO_UP) < 0) {
                 return reorganizeSpace(con, seq, sourceMode, destMode, nodeInfo.getParentId(), includeNodeId, overrideSpacing, overrideLeft, insertParent,
                         insertPosition, insertSpace, insertBoundaries, depthDelta, destinationNode, createMode, createKeepIds, firstCreatedNodeId);
             }
         }
 
         //long time = System.currentTimeMillis();
         Statement stmt = null;
         PreparedStatement ps = null;
         ResultSet rs;
         BigDecimal left = overrideLeft == null ? nodeInfo.getLeft() : overrideLeft;
         BigDecimal right = null;
         String includeNode = includeNodeId ? "=" : "";
         long counter = 0;
         firstCreatedNodeId = -1L;
         long newId = -1;
         try {
             String createProps = createMode ? ",PARENT,REF,NAME,TEMPLATE" : "";
             String sql = " SELECT ID,TOTAL_CHILDCOUNT,CHILDCOUNT, LFT LFTORD,RGT,DEPTH" + createProps +
                     " FROM (SELECT ID,TOTAL_CHILDCOUNT,CHILDCOUNT,LFT,RGT,DEPTH" + createProps + " FROM " + getTable(sourceMode) + " WHERE " +
                     "LFT>" + includeNode + nodeInfo.getLeft() + " AND LFT<" + includeNode + nodeInfo.getRight() + ") NODE " +
                     "ORDER BY LFTORD ASC";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             //long time2 = System.currentTimeMillis();
             if (createMode) {
                 ps = con.prepareStatement("INSERT INTO " + getTable(destMode) + " (ID,PARENT,DEPTH,DIRTY,REF,TEMPLATE,LFT,RGT," +
                         "TOTAL_CHILDCOUNT,CHILDCOUNT,NAME,MODIFIED_AT) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,UNIX_TIMESTAMP()*1000)");
             } else {
                 ps = con.prepareStatement("UPDATE " + getTable(sourceMode) + " SET LFT=?,RGT=?,DEPTH=? WHERE ID=?");
             }
             long id;
             int total_childs;
             int direct_childs;
             BigDecimal nextLeft;
             int lastDepth = nodeInfo.getDepth() + (includeNodeId ? 0 : 1);
             int depth;
             BigDecimal _rgt;
             BigDecimal _lft;
             Long ref = null;
             String template = null;
             String name = "";
 
             Stack<Long> currentParent = null;
             if (createMode) {
                 currentParent = new Stack<Long>();
                 currentParent.push(destinationNode);
             }
 
             //System.out.println("Spacing:"+SPACING);
             while (rs.next()) {
                 //System.out.println("------------------");
                 id = rs.getLong(1);
                 total_childs = rs.getInt(2);
                 direct_childs = rs.getInt(3);
                 _lft = rs.getBigDecimal(4);
                 _rgt = rs.getBigDecimal(5);
                 depth = rs.getInt(6);
                 if (createMode) {
                     // Reading this properties is slow, only do it when needed
                     ref = rs.getLong(8);
                     if (rs.wasNull()) ref = null;
                     name = rs.getString(9);
                     template = rs.getString(10);
                     if (rs.wasNull()) template = null;
                 }
                 left = left.add(SPACING).add(BigDecimal.ONE);
 
                 // Handle depth differences
                 if (lastDepth - depth > 0) {
                     BigDecimal depthDifference = SPACING.add(BigDecimal.ONE);
                     left = left.add(depthDifference.multiply(new BigDecimal(lastDepth - depth)));
                 }
                 if (createMode) {
                     if (lastDepth < depth) {
                         currentParent.push(newId);
                     } else if (lastDepth > depth) {
                         currentParent.pop();
                     }
                 }
 
                 right = left.add(SPACING).add(BigDecimal.ONE);
 
                 // add child space if needed
                 if (total_childs > 0) {
                     BigDecimal childSpace = SPACING.multiply(new BigDecimal(total_childs * 2));
                     childSpace = childSpace.add(new BigDecimal((total_childs * 2) - 1));
                     right = right.add(childSpace);
                     nextLeft = left;
                 } else {
                     nextLeft = right;
                 }
 
                 //System.out.println(id+": "+left+"--"+right);
                 if (insertBoundaries != null) {
                     if (_lft.compareTo(insertBoundaries[0]) > 0) {
                         left = left.add(insertSpace);
                         //System.out.println("Adding space to left");
                     }
                     if (_rgt.compareTo(insertBoundaries[0]) > 0) {
                         right = right.add(insertSpace);
                         //System.out.println("Adding space to right");
                     }
                 }
 
                 // Update the node
                 if (createMode) {
                     newId = createKeepIds ? id : seq.getId(destMode.getSequencer());
                     if (firstCreatedNodeId == -1) {
                         firstCreatedNodeId = newId;
                     }
                     // Create the main entry
                     ps.setLong(1, newId);
                     ps.setLong(2, currentParent.peek());
                     ps.setLong(3, depth + depthDelta);
                     ps.setBoolean(4, destMode != FxTreeMode.Live); //only flag non-live tree's dirty
                     if (ref == null) {
                         ps.setNull(5, java.sql.Types.NUMERIC);
                     } else {
                         ps.setLong(5, ref);
                     }
                     if (template == null) {
                         ps.setNull(6, java.sql.Types.VARCHAR);
                     } else {
                         ps.setString(6, template);
                     }
 //                    System.out.println("=> id:"+newId+" left:"+left+" right:"+right);
                     ps.setBigDecimal(7, left);
                     ps.setBigDecimal(8, right);
                     ps.setInt(9, total_childs);
                     ps.setInt(10, direct_childs);
                     ps.setString(11, name);
                     ps.addBatch();
                 } else {
                     ps.setBigDecimal(1, left);
                     ps.setBigDecimal(2, right);
                     ps.setInt(3, depth + depthDelta);
                     ps.setLong(4, id);
                     ps.addBatch();
                     ps.executeBatch();
                     ps.clearBatch();
                 }
 
                 // Prepare variables for the next node
                 left = nextLeft;
                 lastDepth = depth;
                 counter++;
 
                 // Execute batch every 10000 items to avoid out of memory
                 if (counter % 10000 == 0) {
                     ps.executeBatch();
                     ps.clearBatch();
                 }
             }
             rs.close();
             stmt.close();
             stmt = null;
             ps.executeBatch();
 
             //System.out.println("Cleanup: "+counter+" spaceLen="+SPACING+
             //        " time:"+(System.currentTimeMillis()-time)+
             //        " time2:"+(System.currentTimeMillis()-time2));
             return true;
         } catch (Throwable e) {
             throw new FxTreeException(LOG, e, "ex.tree.reorganize.failed", counter, left, right, e.getMessage());
         } finally {
             try {
                 if (stmt != null) stmt.close();
             } catch (Throwable t) {/*ignore*/}
             try {
                 if (ps != null) ps.close();
             } catch (Throwable t) {/*ignore*/}
         }
     }
 
     /**
      * Helper function to create a new node.
      *
      * @param con          an open and valid connection
      * @param seq          reference to a sequencer
      * @param ce           reference to the content engine
      * @param mode         Live or Edit mode
      * @param parentNodeId the parent node (1=root)
      * @param name         the name of the new node (only informative value)
      * @param label        label for Caption property (only used if new reference is created)
      * @param position     the position within the childs (0 based, Integer.MAX_VALUE may be used to
      *                     append to the end)
      * @param reference    a reference to an existing content (must exist!)
      * @param nodeId       the id to use or create a new one if < 0
      * @param template     the template reference id, or null
      * @return the used or created node id
      * @throws FxTreeException if the function fails
      */
     private long _createNode(Connection con, SequencerEngine seq, ContentEngine ce, FxTreeMode mode, long parentNodeId, String name,
                              FxString label, int position, FxPK reference, String template, long nodeId)
             throws FxApplicationException {
 //        makeSpace(con, seq/*irrelevant*/, mode, parentNodeId, position/*irrelevant*/, 1);
         FxTreeNodeInfoSpreaded parentNode = (FxTreeNodeInfoSpreaded) getTreeNodeInfo(con, mode, parentNodeId);
         BigDecimal boundaries[] = getBoundaries(con, parentNode, position);
         BigDecimal leftBoundary = boundaries[0]; //== left border
         BigDecimal rightBoundary = boundaries[1]; //== right border
 
         // Node has to be inserted between the left and right boundary and needs 2 slots for its left and right border
         BigDecimal spacing = rightBoundary.subtract(leftBoundary).subtract(TWO);
         // Compute spacing for left,inner and right part
         spacing = spacing.divide(THREE, RoundingMode.FLOOR);
 
         // We need at least 2 open slots (for the left and right boundary of the new node)
         //if the spacing is <= 0 we need more space
         if (spacing.compareTo(BigDecimal.ZERO) <= 0/*less than*/) {
             throw new FxTreeException("ex.tree.create.noSpace", parentNodeId);
         }
 
         BigDecimal left = leftBoundary.add(spacing).add(BigDecimal.ONE);
         BigDecimal right = left.add(spacing).add(BigDecimal.ONE);
 
         NodeCreateInfo nci = getNodeCreateInfo(mode, seq, ce, nodeId, name, label, reference);
 
         // Create the node
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement("INSERT INTO " + getTable(mode) + " (ID,PARENT,DEPTH,DIRTY,REF,LFT,RGT," +
                     "TOTAL_CHILDCOUNT,CHILDCOUNT,NAME,MODIFIED_AT,TEMPLATE) VALUES " +
                     "(" + nci.id + "," + parentNodeId + "," + (parentNode.getDepth() + 1) +
                     ",?," + nci.reference.getId() + ",?,?,0,0,?,UNIX_TIMESTAMP()*1000,?)");
             ps.setBoolean(1, mode != FxTreeMode.Live);
             ps.setBigDecimal(2, left);
             ps.setBigDecimal(3, right);
             ps.setString(4, nci.name);
             if (StringUtils.isEmpty(template)) {
                 ps.setNull(5, java.sql.Types.VARCHAR);
             } else {
                 ps.setString(6, template);
             }
             ps.executeUpdate();
             ps.close();
 
             // Update all affected childcounts
             ps = con.prepareStatement("UPDATE " + getTable(mode) + " SET TOTAL_CHILDCOUNT=TOTAL_CHILDCOUNT+1 WHERE " +
                     " LFT<? AND RGT>?");
             ps.setBigDecimal(1, left);
             ps.setBigDecimal(2, right);
             ps.executeUpdate();
             ps.close();
             //update the parents childcount
             ps = con.prepareStatement("UPDATE " + getTable(mode) + " SET CHILDCOUNT=CHILDCOUNT+1 WHERE ID=" + parentNodeId);
             ps.executeUpdate();
         } catch (SQLException e) {
             throw new FxTreeException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             try {
                 if (ps != null) ps.close();
             } catch (Throwable t) {
                 /*ignore*/
             }
         }
         return nci.id;
     }
 
     /**
      * {@inheritDoc}
      */
     public long createNode(Connection con, SequencerEngine seq, ContentEngine ce, FxTreeMode mode, long nodeId, long parentNodeId, String name,
                            FxString label, int position, FxPK reference, String template) throws FxApplicationException {
         checkTemplateValue(template);
         try {
             return _createNode(con, seq, ce, mode, parentNodeId, name, label, position, reference, template, nodeId);
         } catch (FxTreeException e) {
             if ("ex.tree.create.noSpace".equals(e.getExceptionMessage().getKey())) {
                 reorganizeSpace(con, seq, mode, mode, parentNodeId, false, null, null, null, -1, null, null, 0, null, false, false, null);
                 return _createNode(con, seq, ce, mode, parentNodeId, name, label, position, reference, template, nodeId);
             } else
                 throw e;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void move(Connection con, SequencerEngine seq, FxTreeMode mode, long nodeId, long newParentId, int newPosition) throws FxApplicationException {
 
         // Check both nodes (this throws an Exception if they do not exist)
         FxTreeNodeInfo node = getTreeNodeInfo(con, mode, nodeId);
         FxTreeNodeInfoSpreaded destinationNode = (FxTreeNodeInfoSpreaded) getTreeNodeInfo(con, mode, newParentId);
         final FxTreeNodeInfo parent = getTreeNodeInfo(con, mode, node.getParentId());
 
         final long currentPos = node.getPosition();
 
         // Sanity checks for the position
         if (newPosition < 0) {
             newPosition = 0;
         } else if (newPosition > parent.getDirectChildCount()) {
             newPosition = parent.getDirectChildCount() == 0 ? 1 : parent.getDirectChildCount();
         }
 
         final boolean getsNewParent = node.getParentId() != newParentId;
 
         // Take ourself into account if the node stays at the same level
         //System.out.println("newPos:"+newPosition);
         if (!getsNewParent) {
             if (node.getPosition() == newPosition) {
                 // Nothing to do at all
                 return;
             } else if (newPosition < currentPos) {
                 //newPosition = newPosition - 1;
             } else {
                 newPosition = newPosition + 1;
             }
         }
         if (newPosition < 0) newPosition = 0;
         //System.out.println("newPosX:"+newPosition);
 
         final long oldParent = node.getParentId();
 
         // Node may not be moved inside itself!
         if (nodeId == newParentId || node.isParentOf(destinationNode)) {
             throw new FxTreeException("ex.tree.move.recursion", nodeId);
         }
 
         // Make space for the new nodes
         BigDecimal spacing = makeSpace(con, seq, mode, newParentId, newPosition, node.getTotalChildCount() + 1);
 
         // Reload the node to obtain the new boundary and spacing informations
         destinationNode = (FxTreeNodeInfoSpreaded) getTreeNodeInfo(con, mode, newParentId);
         BigDecimal boundaries[] = getBoundaries(con, destinationNode, newPosition);
 
         // Move the nodes
         int depthDelta = (destinationNode.getDepth() + 1) - node.getDepth();
         reorganizeSpace(con, seq, mode, mode, node.getId(), true, spacing, boundaries[0], null, -1, null, null,
                 depthDelta, null, false, false, null);
 
 
         Statement stmt = null;
         try {
             // Update the parent of the node
             stmt = con.createStatement();
             stmt.addBatch("UPDATE " + getTable(mode) + " SET PARENT=" + newParentId + " WHERE ID=" + nodeId);
             if (mode != FxTreeMode.Live)
                 stmt.addBatch("UPDATE " + getTable(mode) + " SET DIRTY=TRUE WHERE ID=" + nodeId);
             stmt.executeBatch();
             stmt.close();
 
             // Update the childcount of the new and old parent if needed + set dirty flag
             if (getsNewParent) {
                 // Reload the nodes to obtain the new boundary and spacing informations
                 destinationNode = (FxTreeNodeInfoSpreaded) getTreeNodeInfo(con, mode, newParentId);
                 node = getTreeNodeInfo(con, mode, nodeId);
                 FxTreeNodeInfo nodeOldParent = getTreeNodeInfo(con, mode, oldParent);
                 stmt = con.createStatement();
                 stmt.addBatch("UPDATE " + getTable(mode) + " SET TOTAL_CHILDCOUNT=TOTAL_CHILDCOUNT+" + (node.getTotalChildCount() + 1) +
                         " WHERE (LFT<=" + destinationNode.getLeft() + " AND RGT>=" + destinationNode.getRight() + ")");
                 stmt.addBatch("UPDATE " + getTable(mode) + " SET TOTAL_CHILDCOUNT=TOTAL_CHILDCOUNT-" + (node.getTotalChildCount() + 1) +
                         " WHERE (LFT<=" + nodeOldParent.getLeft() + " AND RGT>=" + nodeOldParent.getRight() + ")");
                 stmt.addBatch("UPDATE " + getTable(mode) + " SET CHILDCOUNT=CHILDCOUNT+1 WHERE ID=" + newParentId);
                 stmt.addBatch("UPDATE " + getTable(mode) + " SET CHILDCOUNT=CHILDCOUNT-1 WHERE ID=" + oldParent);
                 if (mode != FxTreeMode.Live) {
                     stmt.addBatch("UPDATE " + getTable(mode) + " SET DIRTY=TRUE WHERE LFT>=" + node.getLeft() + " AND " +
                             "RGT<=" + node.getRight());
                     stmt.addBatch("UPDATE " + getTable(mode) + " SET DIRTY=TRUE WHERE ID IN(" + oldParent + "," + newParentId + ")");
                 }
                 stmt.executeBatch();
                 stmt.close();
             }
 
         } catch (SQLException e) {
             throw new FxTreeException("ex.tree.move.parentUpdate.failed", node.getId(), e.getMessage());
         } finally {
             try {
                 if (stmt != null) stmt.close();
             } catch (Exception exc) {
                 //ignore
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public long copy(Connection con, SequencerEngine seq, FxTreeMode mode, long srcNodeId, long dstParentNodeId,
                      int dstPosition, boolean deepReferenceCopy, String copyOfPrefix) throws FxApplicationException {
         // Check both nodes (this throws a FxNotFoundException if they do not exist)
         final FxTreeNodeInfo sourceNode = getTreeNodeInfo(con, mode, srcNodeId);
         getTreeNodeInfo(con, mode, dstParentNodeId);
 
         // Make space for the new nodes
         BigDecimal spacing = makeSpace(con, seq, mode, dstParentNodeId, dstPosition, sourceNode.getTotalChildCount() + 1);
 
         // Reload the node to obtain the new boundary and spacing informations
         final FxTreeNodeInfoSpreaded destinationNode = (FxTreeNodeInfoSpreaded) getTreeNodeInfo(con, mode, dstParentNodeId);
 
         // Copy the data
         BigDecimal boundaries[] = getBoundaries(con, destinationNode, dstPosition);
         int depthDelta = (destinationNode.getDepth() + 1) - sourceNode.getDepth();
         Long firstCreatedNodeId = -1L;
         reorganizeSpace(con, seq, mode, mode, sourceNode.getId(), true, spacing, boundaries[0], null, -1, null, null,
                 depthDelta, dstParentNodeId, true, false, firstCreatedNodeId);
 
         Statement stmt = null;
         try {
             // Update the childcount of the new parents
             stmt = con.createStatement();
             stmt.addBatch("UPDATE " + getTable(mode) + " SET TOTAL_CHILDCOUNT=TOTAL_CHILDCOUNT+" + (sourceNode.getTotalChildCount() + 1) +
                     " WHERE (LFT<=" + destinationNode.getLeft() + " AND RGT>=" + destinationNode.getRight() + ")");
             stmt.addBatch("UPDATE " + getTable(mode) + " SET CHILDCOUNT=CHILDCOUNT+1 WHERE ID=" + dstParentNodeId);
             stmt.executeBatch();
 
             if (deepReferenceCopy) {
                 //TODO: clone all references of this node and all children
                 if (true)
                     throw new FxApplicationException("ex.general.notImplemented", "Deep reference copy of tree nodes");
 
                 int copyOfNr = getCopyOfCount(con, mode, copyOfPrefix, dstParentNodeId, firstCreatedNodeId);
                 // Make sure the name is unique
                 stmt.executeUpdate("UPDATE " + getTable(mode) + " SET NAME=CONCAT(CONCAT('" + copyOfPrefix + "',NAME),'(" +
                         copyOfNr + ")') WHERE ID=" + (long) firstCreatedNodeId);
             }
 
         } catch (SQLException exc) {
             throw new FxTreeException("MoveNode: Failed to update the parent of node#" + srcNodeId + ": " + exc.getMessage());
         } finally {
             try {
                 if (stmt != null) stmt.close();
             } catch (Exception exc) {
                 //ignore
             }
         }
         return firstCreatedNodeId;
     }
 
     /**
      * {@inheritDoc}
      */
     public void activateNode(Connection con, SequencerEngine seq, ContentEngine ce, FxTreeMode mode, final long nodeId) throws FxApplicationException {
         if (mode == FxTreeMode.Live) //Live tree can not be activated!
             return;
         long ids[] = getIdChain(con, mode, nodeId);
         for (long id : ids) {
             if (id == ROOT_NODE) continue;
             FxTreeNode srcNode = getNode(con, mode, id);
             //check if the node already exists in the live tree
             if (exists(con, FxTreeMode.Live, id)) {
                 //Move and setTemplate will not do anything if the node is already in its correct place and
                 move(con, seq, FxTreeMode.Live, id, srcNode.getParentNodeId(), srcNode.getPosition());
                 setTemplate(con, FxTreeMode.Live, id, srcNode.getTemplate());
             } else {
                 createNode(con, seq, ce, FxTreeMode.Live, srcNode.getId(), srcNode.getParentNodeId(),
                         srcNode.getName(), srcNode.getLabel(), srcNode.getPosition(),
                         srcNode.getReference(), srcNode.getTemplate());
             }
 
             // Remove all deleted direct child nodes
             Statement stmt = null;
             Statement stmt2 = null;
             try {
                 stmt = con.createStatement();
                 stmt2 = con.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DISTINCT a.ID FROM (SELECT ID FROM " + getTable(FxTreeMode.Live) + " WHERE PARENT=" +
                         nodeId + ") a LEFT \n" +
                         "JOIN (SELECT ID FROM " + getTable(FxTreeMode.Live) + " WHERE PARENT=" + nodeId + ") b USING (ID) WHERE b.ID IS NULL");
                 while (rs != null && rs.next()) {
                     long deleteId = rs.getLong(1);
                     stmt2.addBatch("SET FOREIGN_KEY_CHECKS=0");
                     stmt2.addBatch("DELETE FROM " + getTable(FxTreeMode.Live) + " WHERE ID=" + deleteId);
                     stmt2.addBatch("SET FOREIGN_KEY_CHECKS=1");
                 }
                 stmt2.executeBatch();
             } catch (SQLException e) {
                 throw new FxTreeException("ex.tree.activate.failed", nodeId, false, e.getMessage());
             } finally {
                 try {
                     if (stmt != null) stmt.close();
                 } catch (Exception exc) {
                     //ignore
                 }
                 try {
                     if (stmt2 != null) stmt2.close();
                 } catch (Exception exc) {
                     //ignore
                 }
             }
             clearDirtyFlag(con, mode, nodeId);
         }
     }
 
     /**
      * Activate a node, its subtree and its parents up to the root node
      *
      * @param con    an open and valid connection
      * @param seq    reference to the sequencer
      * @param ce     reference to the content engine
      * @param mode   tree mode
      * @param nodeId node id
      * @throws FxApplicationException on errors
      */
     public void activateSubtree(Connection con, SequencerEngine seq, ContentEngine ce, FxTreeMode mode, long nodeId)
             throws FxApplicationException {
 
         if (nodeId == ROOT_NODE) {
             activateAll(con, mode);
             return;
         }
 
         final FxTreeNodeInfo sourceNode = getTreeNodeInfo(con, mode, nodeId);
         final long destination = sourceNode.getParentId();
 
         // Make sure the path up to the root node is activated
         activateNode(con, seq, ce, mode, sourceNode.getParentId());
 
         //***************************************************************
         //* Cleanup all affected nodes
         //***************************************************************
 
         // First we clear all affected nodes in the live tree, since we will copy them from the edit tree.
         // We also need to delete all nodes that are children of the specified node in the edit tree, since they
         // were moved into our new subtree.
         Statement stmt = null;
         try {
             String sql = "SELECT ID FROM " + getTable(FxTreeMode.Live) +
                     " WHERE (LFT>=" + sourceNode.getLeft() + " AND RGT<=" + sourceNode.getRight() + ") OR ID=" + nodeId;
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             while (rs.next()) {
                 try {
                     removeNode(con, FxTreeMode.Live, ce, rs.getLong(1), true);
                 } catch (FxNotFoundException nf) {
                     // node is not live yet - thats ok
                 }
             }
             stmt.close();
         } catch (SQLException exc) {
             throw new FxTreeException("ex.tree.activate.failed", nodeId, true, exc.getMessage());
         } finally {
             try {
                 if (stmt != null) stmt.close();
             } catch (Exception exc) {/*ignore*/}
         }
 
         //***************************************************************
         //* Now we can copy all affected nodes to the live tree
         //***************************************************************
 
         int position = 0;
 
         // Make space for the new nodes
         BigDecimal spacing = makeSpace(con, seq, FxTreeMode.Live, destination, /*sourceNode.getPosition()*/position,
                 sourceNode.getTotalChildCount() + 1);
 
         // Reload the node to obtain the new boundary and spacing informations
         FxTreeNodeInfoSpreaded destinationNode = (FxTreeNodeInfoSpreaded) getTreeNodeInfo(con, FxTreeMode.Live, destination);
 
         // Copy the data
         BigDecimal boundaries[] = getBoundaries(con, destinationNode, position);
         int depthDelta = (destinationNode.getDepth() + 1) - sourceNode.getDepth();
         reorganizeSpace(con, seq, mode, FxTreeMode.Live, sourceNode.getId(), true, spacing,
                 boundaries[0], null, 0, null, null, depthDelta, destination, true, true, null);
 
         try {
             // Update the childcount of the new parents
             stmt = con.createStatement();
             stmt.addBatch("UPDATE " + getTable(FxTreeMode.Live) + " SET TOTAL_CHILDCOUNT=TOTAL_CHILDCOUNT+" + (sourceNode.getTotalChildCount() + 1) +
                     " WHERE (LFT<=" + destinationNode.getLeft() + " AND RGT>=" + destinationNode.getRight() + ")");
             stmt.addBatch("UPDATE " + getTable(FxTreeMode.Live) + " SET CHILDCOUNT=CHILDCOUNT+1 WHERE ID=" + destination);
             stmt.addBatch("UPDATE " + getTable(mode) + " SET DIRTY=FALSE WHERE LFT>=" + sourceNode.getLeft() + " AND RGT<=" + sourceNode.getRight());
             stmt.executeBatch();
             stmt.close();
         } catch (SQLException exc) {
             throw new FxTreeException("ex.tree.activate.failed", nodeId, true, exc.getMessage());
         } finally {
             try {
                 if (stmt != null) stmt.close();
             } catch (Exception exc) {/*ignore*/}
         }
         // Make sure the node is at the correct position
         move(con, seq, FxTreeMode.Live, sourceNode.getId(), sourceNode.getParentId(), sourceNode.getPosition());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void wipeTree(FxTreeMode mode, Statement stmt, FxPK rootPK) throws SQLException {
         stmt.execute("SET FOREIGN_KEY_CHECKS=0");
         stmt.executeUpdate("DELETE FROM " + getTable(mode));
         stmt.executeUpdate("INSERT INTO " + getTable(mode) + " (ID,NAME,MODIFIED_AT,DIRTY,PARENT,DEPTH,CHILDCOUNT,TOTAL_CHILDCOUNT,REF,TEMPLATE,LFT,RGT) " +
                 "VALUES (" + ROOT_NODE + ",'Root',UNIX_TIMESTAMP()*1000,FALSE,NULL,1,0,0," + rootPK.getId() +
                 ",NULL,1," + MAX_RIGHT + ")");
         stmt.executeUpdate("SET FOREIGN_KEY_CHECKS=1");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void afterNodeRemoved(Connection con, FxTreeNodeInfo nodeInfo, boolean removeChildren) {
         //nothing to do in spreaded mode
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void checkTree(Connection con, FxTreeMode mode) throws FxApplicationException {
         Statement stmt = null;
         Statement stmt2 = null;
         try {
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ID FROM " + getTable(mode));
             long nodes = 0;
             while (rs.next()) {
                 Long id = rs.getLong(1);
                 FxTreeNodeInfoSpreaded node = (FxTreeNodeInfoSpreaded) getTreeNodeInfo(con, mode, id);
                 stmt2 = con.createStatement();
                 ResultSet rs2 = stmt2.executeQuery("SELECT MAX(LFT),MAX(RGT),MIN(LFT),MIN(RGT) FROM " +
                         getTable(mode) + " WHERE PARENT=" + id);
                 rs2.next();
                 BigDecimal maxLft = rs2.getBigDecimal(1);
                 BigDecimal maxRgt = rs2.getBigDecimal(2);
                 BigDecimal minLft = rs2.getBigDecimal(3);
                 BigDecimal minRgt = rs2.getBigDecimal(4);
                 stmt2.close();
                 if (maxLft != null) {
                     BigDecimal max = maxLft.max(maxRgt).max(minLft).max(minRgt);
                     BigDecimal min = maxLft.min(maxRgt).min(minLft).min(minRgt);
                     if (max.compareTo(node.getRight()) > 0)
                         throw new FxTreeException(LOG, "ex.tree.check.failed", mode, "#" + id + " out of bounds (right)");
                     if (min.compareTo(node.getLeft()) < 0)
                         throw new FxTreeException(LOG, "ex.tree.check.failed", mode, "#" + id + " out of bounds (left)");
                 }
 
                 // Total child count check
                 stmt2 = con.createStatement();
                 rs2 = stmt2.executeQuery("SELECT COUNT(*) FROM " +
                         getTable(mode) + " WHERE LFT>" + node.getLeft() + " AND RGT<" + node.getRight());
                 rs2.next();
                 int totalChilds = rs2.getInt(1);
                 if (totalChilds != node.getTotalChildCount())
                     throw new FxTreeException(LOG, "ex.tree.check.failed", mode, "#" + id + " invalid total child count [" + totalChilds + "!=" + node.getTotalChildCount() + "]");
                 stmt2.close();
 
                 // Direct child count check
                 stmt2 = con.createStatement();
                 rs2 = stmt2.executeQuery("SELECT COUNT(*) FROM " + getTable(mode) + " WHERE PARENT=" + id);
                 rs2.next();
                 int directChilds = rs2.getInt(1);
                 if (directChilds != node.getDirectChildCount())
                     throw new FxTreeException(LOG, "ex.tree.check.failed", mode, "#" + id + " invalid direct child count [" + directChilds + "!=" + node.getDirectChildCount() + "]");
                 if (directChilds > node.getTotalChildCount())
                     throw new FxTreeException(LOG, "ex.tree.check.failed", mode, "#" + id +
                             " more direct than total children! [direct:" + directChilds + ">total:" + node.getTotalChildCount() + "]");
                 stmt2.close();
 
                 // Depth check
                 if (!node.isRoot()) {
                     stmt2 = con.createStatement();
                     rs2 = stmt2.executeQuery("SELECT DEPTH FROM " + getTable(mode) + " WHERE ID=" + node.getParentId());
                     rs2.next();
                     int depth = rs2.getInt(1);
                     stmt2.close();
                     if ((node.getDepth() - 1) != depth)
                         throw new FxTreeException(LOG, "ex.tree.check.failed", mode, "#" + id + " invalid depth: " + node.getDepth() + ", parent depth=" + depth);
                 }
                 nodes++;
             }
             if (LOG.isDebugEnabled())
                 LOG.debug("Successfully checked [" + nodes + "] tree nodes in mode [" + mode.name() + "]!");
         } catch (SQLException e) {
             throw new FxTreeException(LOG, e, "ex.tree.check.failed", mode, e.getMessage());
         } finally {
             try {
                 if (stmt != null) stmt.close();
             } catch (Exception exc) {/*ignore*/}
             try {
                 if (stmt2 != null) stmt2.close();
             } catch (Exception exc) {/*ignore*/}
         }
     }
 }
