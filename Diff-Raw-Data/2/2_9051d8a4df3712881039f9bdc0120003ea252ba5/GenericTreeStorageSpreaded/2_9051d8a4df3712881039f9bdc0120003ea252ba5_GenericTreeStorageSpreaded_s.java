 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
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
 
 import com.flexive.core.Database;
 import static com.flexive.core.DatabaseConst.TBL_CONTENT;
 import com.flexive.core.storage.DBStorage;
 import com.flexive.core.storage.FxTreeNodeInfo;
 import com.flexive.core.storage.FxTreeNodeInfoSpreaded;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.content.*;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.ContentEngine;
 import com.flexive.shared.interfaces.SequencerEngine;
 import com.flexive.shared.structure.FxType;
 import com.flexive.shared.tree.FxTreeMode;
 import com.flexive.shared.tree.FxTreeNode;
 import com.flexive.shared.value.FxString;
 import com.google.common.collect.Iterables;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.sql.*;
 import java.util.List;
 import java.util.Stack;
 import java.util.Arrays;
 
 /**
  * Generic tree storage implementation using a spreaded nested set tree
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @author Gregor Schober (gregor.schober@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class GenericTreeStorageSpreaded extends GenericTreeStorage {
     private static final Log LOG = LogFactory.getLog(GenericTreeStorageSpreaded.class);
 
     protected static final BigDecimal TWO = new BigDecimal(2);
     protected static final BigDecimal THREE = new BigDecimal(3);
     protected static final BigDecimal GO_UP = new BigDecimal(1024);
     protected static final BigDecimal MAX_RIGHT = new BigDecimal("18446744073709551615");
 
     /**
      * The maximum spacing for new nodes. Lower means less space reorgs for flat lists, but more reorgs for
      * deeply nested trees.
      */
     protected static final BigDecimal DEFAULT_NODE_SPACING = new BigDecimal(10000);
 //    protected static final BigDecimal MAX_RIGHT = new BigDecimal("1000");
 //    protected static final BigDecimal GO_UP = new BigDecimal(10);
 
     private static final String TREE_LIVE_MAXRIGHT = "SELECT MAX(RGT) FROM " + getTable(FxTreeMode.Live) +
             //            1
             " WHERE PARENT=?";
     private static final String TREE_EDIT_MAXRIGHT = "SELECT MAX(RGT) FROM " + getTable(FxTreeMode.Edit) +
             //            1
             " WHERE PARENT=?";
     private static final Object LOCK_REORG = new Object();
 
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
             ps = con.prepareStatement(prepareSql(mode, mode == FxTreeMode.Live ? TREE_LIVE_NODEINFO : TREE_EDIT_NODEINFO));
             ps.setBoolean(1, mode == FxTreeMode.Live);
             ps.setLong(2, nodeId);
             ps.setBoolean(3, true);
             rs = ps.executeQuery();
             if (rs == null || !rs.next())
                 throw new FxNotFoundException("ex.tree.node.notFound", nodeId, mode);
             FxType _type = CacheAdmin.getEnvironment().getType(rs.getLong(15));
             long _stepACL = CacheAdmin.getEnvironment().getStep(rs.getLong(17)).getAclId();
             long _createdBy = rs.getLong(18);
             long _mandator = rs.getLong(19);
             final FxPK reference = new FxPK(rs.getLong(9), rs.getInt(16));
             final List<Long> aclIds = fetchNodeACLs(con, reference);
             return new FxTreeNodeInfoSpreaded(rs.getBigDecimal(1), rs.getBigDecimal(2), rs.getBigDecimal(5),
                     rs.getBigDecimal(6), maxRight, rs.getInt(4), rs.getInt(8), rs.getInt(7), rs.getLong(3),
                     nodeId, rs.getString(12), reference,
                     aclIds, mode, rs.getInt(13), rs.getString(10), rs.getLong(11),
                     FxPermissionUtils.getPermissionUnion(aclIds, _type, _stepACL, _createdBy, _mandator));
         } catch (SQLException e) {
             throw new FxTreeException(e, "ex.tree.nodeInfo.sqlError", nodeId, e.getMessage());
         } finally {
             Database.closeObjects(GenericTreeStorageSpreaded.class, null, ps);
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
                     StorageManager.getLimitOffset(false, 2, (position == 0) ? 0 : position - 1));
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
                 insertSpace, boundaries, 0, null, false, false);
         return spacing;
     }
 
     /**
      * Do what i mean function :-D
      *
      * @param con              an open and valid Connection
      * @param seq              a valid Sequencer reference
      * @param sourceMode       the source table (matters in createMode only)
      * @param destMode         the destination table (matters in createMode only)
      * @param nodeId           the node to work on
      * @param includeNodeId    if true the operations root node (nodeId) is included into the updates
      * @param overrideSpacing  if set this spacing is used instead of the computed one
      * @param overrideLeft     if set this will be the first left position
      * @param insertParent     create mode only: the parent node in which we will generate the free space
      *                         specified by the parameters [insertPosition] and [insertSpace]
      * @param insertPosition   create mode only: the position withn the destination nodes childs
      * @param insertSpace      create mode only: the space to keep free at the specified position
      * @param insertBoundaries create mode only: the insert boundaries
      * @param depthDelta       create mode only: the delta to apply to the depth
      * @param destinationNode  create mode only: the destination node
      * @param createMode       if true the function will insert copy of nodes instead of updating them
      * @param createKeepIds    keep the ids in create mode
      * @return first created node id or -1 if no node was created using this method
      * @throws FxTreeException if the function fails
      */
     public long reorganizeSpace(Connection con, SequencerEngine seq,
                                 FxTreeMode sourceMode, FxTreeMode destMode,
                                 long nodeId, boolean includeNodeId, BigDecimal overrideSpacing, BigDecimal overrideLeft,
                                 FxTreeNodeInfo insertParent, int insertPosition, BigDecimal insertSpace, BigDecimal insertBoundaries[],
                                 int depthDelta, Long destinationNode, boolean createMode, boolean createKeepIds) throws FxTreeException {
         Statement stmt = null;
         try {
             synchronized (LOCK_REORG) {
                 acquireLocksForUpdate(con, sourceMode);
                 stmt = con.createStatement();
                 stmt.execute(StorageManager.getReferentialIntegrityChecksStatement(false));
                 return _reorganizeSpace(con, seq, sourceMode, destMode, nodeId, includeNodeId, overrideSpacing,
                         overrideLeft, insertParent, insertPosition, insertSpace, insertBoundaries,
                         depthDelta, destinationNode, createMode, createKeepIds);
             }
         } catch (FxDbException e) {
             throw new FxTreeException(e);
         } catch (SQLException e) {
             throw new FxTreeException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             if (stmt != null) {
                 try {
                     try {
                         stmt.execute(StorageManager.getReferentialIntegrityChecksStatement(true));
                     } catch (SQLException e) {
                         LOG.error(e);
                     }
                     stmt.close();
                 } catch (SQLException e) {
                     LOG.error(e);
                 }
             }
         }
     }
 
     protected long _reorganizeSpace(Connection con, SequencerEngine seq,
                                     FxTreeMode sourceMode, FxTreeMode destMode,
                                     long nodeId, boolean includeNodeId, BigDecimal overrideSpacing, BigDecimal overrideLeft,
                                     FxTreeNodeInfo insertParent, int insertPosition, BigDecimal insertSpace, BigDecimal insertBoundaries[],
                                     int depthDelta, Long destinationNode, boolean createMode, boolean createKeepIds) throws FxTreeException {
         long firstCreatedNodeId = -1;
         FxTreeNodeInfoSpreaded nodeInfo;
         try {
             nodeInfo = (FxTreeNodeInfoSpreaded) getTreeNodeInfo(con, sourceMode, nodeId);
         } catch (Exception e) {
             return -1;
         }
 
         if (!nodeInfo.isSpaceOptimizable()) {
             // The Root node and cant be optimize any more ... so all we can do is fail :-/
             // This should never really happen
             if (nodeId == ROOT_NODE) {
                 return -1;
             }
             //System.out.println("### UP we go");
             return reorganizeSpace(con, seq, sourceMode, destMode, nodeInfo.getParentId(), includeNodeId, overrideSpacing, overrideLeft, insertParent,
                     insertPosition, insertSpace, insertBoundaries, depthDelta, destinationNode, createMode, createKeepIds);
         }
 
         BigDecimal spacing = nodeInfo.getDefaultSpacing();
         if (overrideSpacing != null && (overrideSpacing.compareTo(spacing) < 0 || overrideLeft != null)) {
             // override spacing unless it is greater OR overrideLeft is specified (in that case we
             // have to use the spacing for valid tree ranges)  
             spacing = overrideSpacing;
         } else {
             if (spacing.compareTo(GO_UP) < 0 && !createMode) {
                 return reorganizeSpace(con, seq, sourceMode, destMode, nodeInfo.getParentId(), includeNodeId, overrideSpacing, overrideLeft, insertParent,
                         insertPosition, insertSpace, insertBoundaries, depthDelta, destinationNode, createMode, createKeepIds);
             }
         }
 
         Statement stmt = null;
         PreparedStatement ps = null;
         ResultSet rs;
         BigDecimal left = overrideLeft == null ? nodeInfo.getLeft() : overrideLeft;
         BigDecimal right = null;
         String includeNode = includeNodeId ? "=" : "";
         long counter = 0;
         long newId = -1;
         try {
             final long start = System.currentTimeMillis();
             String createProps = createMode ? ",PARENT,REF,NAME,TEMPLATE" : "";
             String sql = " SELECT ID," +
                     StorageManager.getIfFunction(       // compute total child count only when the node has children
                             "CHILDCOUNT = 0",
                             "0",
                             "(SELECT COUNT(*) FROM " + getTable(sourceMode) + " WHERE LFT > NODE.LFT AND RGT < NODE.RGT)"
                     ) + ", " +
                     "CHILDCOUNT, LFT AS LFTORD,RGT,DEPTH" + createProps +
                     " FROM (SELECT ID,CHILDCOUNT,LFT,RGT,DEPTH" + createProps + " FROM " + getTable(sourceMode) + " WHERE " +
                     "LFT>" + includeNode + nodeInfo.getLeft() + " AND LFT<" + includeNode + nodeInfo.getRight() + ") NODE " +
                     "ORDER BY LFTORD ASC";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             if (createMode) {
                 //                                                                 1  2      3     4     5   6        7   8
                 ps = con.prepareStatement("INSERT INTO " + getTable(destMode) + " (ID,PARENT,DEPTH,DIRTY,REF,TEMPLATE,LFT,RGT," +
                         //9           10    11
                         "CHILDCOUNT,NAME,MODIFIED_AT) " +
                         "VALUES (?,?,?,?,?,?,?,?,?,?,?)");
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
             String data = null;
             String name = "";
 
             Stack<Long> currentParent = null;
             if (createMode) {
                 currentParent = new Stack<Long>();
                 currentParent.push(destinationNode);
             }
 
             //System.out.println("Spacing:"+SPACING);
             int position = 0;
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
                     data = rs.getString(10);
                     if (rs.wasNull()) data = null;
                 }
                 left = left.add(spacing).add(BigDecimal.ONE);
 
                 // Handle depth differences
                 if (lastDepth - depth > 0) {
                     BigDecimal depthDifference = spacing.add(BigDecimal.ONE);
                     left = left.add(depthDifference.multiply(new BigDecimal(lastDepth - depth)));
                 }
                 if (createMode) {
                     if (lastDepth < depth) {
                         currentParent.push(newId);
                     } else if (lastDepth > depth) {
                         for (int p = 0; p < (lastDepth - depth); p++)
                             currentParent.pop();
                     }
                 }
 
                 right = left.add(spacing).add(BigDecimal.ONE);
 
                 // add child space if needed
                 if (total_childs > 0) {
                     BigDecimal childSpace = spacing.multiply(new BigDecimal(total_childs * 2));
                     childSpace = childSpace.add(new BigDecimal((total_childs * 2) - 1));
                     right = right.add(childSpace);
                     nextLeft = left;
                 } else {
                     nextLeft = right;
                 }
 
                 if (insertBoundaries != null && position == insertPosition) {
                     // insert gap at requested position
                     if (_lft.compareTo(insertBoundaries[0]) > 0) {
                         left = left.add(insertSpace);
                         right = right.add(insertSpace);
                         nextLeft = nextLeft.add(insertSpace);
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
                     if (data == null) {
                         ps.setNull(6, java.sql.Types.VARCHAR);
                     } else {
                         ps.setString(6, data);
                     }
 //                    System.out.println("=> id:"+newId+" left:"+left+" right:"+right);
                     ps.setBigDecimal(7, left);
                     ps.setBigDecimal(8, right);
                     ps.setInt(9, direct_childs);
                     ps.setString(10, name);
                     ps.setLong(11, System.currentTimeMillis());
                     ps.addBatch();
                 } else {
                     ps.setBigDecimal(1, left);
                     ps.setBigDecimal(2, right);
                     ps.setInt(3, depth + depthDelta);
                     ps.setLong(4, id);
                     ps.addBatch();
 //                    ps.executeBatch();
 //                    ps.clearBatch();
                 }
 
                 // Prepare variables for the next node
                 left = nextLeft;
                 lastDepth = depth;
                 counter++;
                 if (depth == nodeInfo.getDepth() + 1) {
                     position++; // count position in root folder
                 }
 
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
 
             if (LOG.isDebugEnabled()) {
                 final long time = System.currentTimeMillis() - start;
 
                 LOG.debug("Tree reorganization of " + counter + " items completed in "
                         + time + " ms (spaceLen=" + spacing + ")");
             }
             return firstCreatedNodeId;
         } catch (FxApplicationException e) {
             throw e instanceof FxTreeException ? (FxTreeException) e : new FxTreeException(e);
         } catch (SQLException e) {
             String next = "";
             if (e.getNextException() != null)
                 next = " next:" + e.getNextException().getMessage();
             throw new FxTreeException(LOG, e, "ex.tree.reorganize.failed", counter, left, right, e.getMessage() + next);
         } catch (Exception e) {
             throw new FxTreeException(e);
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
      * @param con             an open and valid connection
      * @param seq             reference to a sequencer
      * @param ce              reference to the content engine
      * @param mode            Live or Edit mode
      * @param parentNodeId    the parent node (1=root)
      * @param name            the name of the new node (only informative value)
      * @param label           label for Caption property (only used if new reference is created)
      * @param position        the position within the childs (0 based, Integer.MAX_VALUE may be used to
      *                        append to the end)
      * @param reference       a reference to an existing content (must exist!)
      * @param data            the optional data
      * @param nodeId          the id to use or create a new one if < 0
      * @param activateContent change the step of contents that have no live step to live in the max version?
      * @return the used or created node id
      * @throws FxTreeException if the function fails
      */
     private long _createNode(Connection con, SequencerEngine seq, ContentEngine ce, FxTreeMode mode, long parentNodeId, String name,
                              FxString label, int position, FxPK reference, String data, long nodeId, boolean activateContent)
             throws FxApplicationException {
 
         // acquire exclusive lock for parent node 
         acquireLocksForUpdate(con, mode, Arrays.asList(parentNodeId));
 
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
 
         // try to use space more efficiently for flat structures, otherwise the first node of a folder
         // will get a third of the subtree space, the second one ninth, and so on.
         // Maxspacing indicates the number of nodes (*2) we expect to put in this node before space reorg
         spacing = spacing.compareTo(DEFAULT_NODE_SPACING) > 0 ? DEFAULT_NODE_SPACING : spacing;
 
 //        final BigDecimal left = leftBoundary.add(spacing).add(BigDecimal.ONE);
         // don't add gap to left boundary (doesn't seem to have any benefits since that space is lost
         // unless the tree is reorganized anyway
         final BigDecimal left = leftBoundary.add(BigDecimal.ONE);
         final BigDecimal right = left.add(spacing).add(BigDecimal.ONE);
 
         NodeCreateInfo nci = getNodeCreateInfo(mode, seq, ce, nodeId, name, label, reference, activateContent);
 
         // Create the node
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement("INSERT INTO " + getTable(mode) + " (ID,PARENT,DEPTH,DIRTY,REF,LFT,RGT," +
                     "CHILDCOUNT,NAME,MODIFIED_AT,TEMPLATE) VALUES " +
                     "(" + nci.id + "," + parentNodeId + "," + (parentNode.getDepth() + 1) +
                     ",?," + nci.reference.getId() + ",?,?,0,?," + StorageManager.getTimestampFunction() + ",?)");
             ps.setBoolean(1, mode != FxTreeMode.Live);
             ps.setBigDecimal(2, left);
             ps.setBigDecimal(3, right);
             ps.setString(4, nci.name);
             if (StringUtils.isEmpty(data)) {
                 ps.setNull(5, java.sql.Types.VARCHAR);
             } else {
                 ps.setString(6, data);
             }
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
                            FxString label, int position, FxPK reference, String data, boolean activateContent) throws FxApplicationException {
         checkDataValue(data);
         try {
             return _createNode(con, seq, ce, mode, parentNodeId, name, label, position, reference, data, nodeId, activateContent);
         } catch (FxTreeException e) {
             if ("ex.tree.create.noSpace".equals(e.getExceptionMessage().getKey())) {
                 reorganizeSpace(con, seq, mode, mode, parentNodeId, false, null, null, null, -1, null, null, 0, null, false, false);
                 return _createNode(con, seq, ce, mode, parentNodeId, name, label, position, reference, data, nodeId, activateContent);
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
 
         acquireLocksForUpdate(con, mode, Arrays.asList(nodeId, newParentId, node.getParentId()));
 
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
                 depthDelta, null, false, false);
 
 
         Statement stmt = null;
         final String TRUE = StorageManager.getBooleanTrueExpression();
         try {
             // Update the parent of the node
             stmt = con.createStatement();
             stmt.addBatch("UPDATE " + getTable(mode) + " SET PARENT=" + newParentId + " WHERE ID=" + nodeId);
             if (mode != FxTreeMode.Live)
                 stmt.addBatch("UPDATE " + getTable(mode) + " SET DIRTY=" + TRUE + " WHERE ID=" + nodeId);
             stmt.executeBatch();
             stmt.close();
 
             // Update the childcount of the new and old parent if needed + set dirty flag
             if (getsNewParent) {
                 node = getTreeNodeInfo(con, mode, nodeId);
                 stmt = con.createStatement();
                 stmt.addBatch("UPDATE " + getTable(mode) + " SET CHILDCOUNT=CHILDCOUNT+1 WHERE ID=" + newParentId);
                 stmt.addBatch("UPDATE " + getTable(mode) + " SET CHILDCOUNT=CHILDCOUNT-1 WHERE ID=" + oldParent);
                 if (mode != FxTreeMode.Live) {
                     final List<Long> newChildren = selectAllChildNodeIds(con, mode, node.getLeft(), node.getRight());
                     acquireLocksForUpdate(con, mode, newChildren);
 
                     for (List<Long> part : Iterables.partition(newChildren, SQL_IN_PARTSIZE)) {
                         stmt.addBatch("UPDATE " + getTable(mode) + " SET DIRTY=" + TRUE +
                                 " WHERE ID IN (" + StringUtils.join(part, ',') + ")");
                     }
 
                     stmt.addBatch("UPDATE " + getTable(mode) + " SET DIRTY=" + TRUE + " WHERE ID IN(" + oldParent + "," + newParentId + ")");
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
 
         acquireLocksForUpdate(con, mode, Arrays.asList(srcNodeId, sourceNode.getParentId(), dstParentNodeId));
 
         // Copy the data
         BigDecimal boundaries[] = getBoundaries(con, destinationNode, dstPosition);
         int depthDelta = (destinationNode.getDepth() + 1) - sourceNode.getDepth();
         long firstCreatedNodeId = reorganizeSpace(con, seq, mode, mode, sourceNode.getId(), true, spacing, boundaries[0], null, -1, null, null,
                 depthDelta, dstParentNodeId, true, false);
 
         Statement stmt = null;
         try {
             // Update the childcount of the new parents
             stmt = con.createStatement();
             stmt.addBatch("UPDATE " + getTable(mode) + " SET CHILDCOUNT=CHILDCOUNT+1 WHERE ID=" + dstParentNodeId);
             stmt.executeBatch();
 
             if (deepReferenceCopy) {
                 //TODO: clone all references of this node and all children
                 throw new FxApplicationException("ex.general.notImplemented", "Deep reference copy of tree nodes");
 /*
                 int copyOfNr = getCopyOfCount(con, mode, copyOfPrefix, dstParentNodeId, firstCreatedNodeId);
                 // Make sure the name is unique
                 stmt.executeUpdate("UPDATE " + getTable(mode) + " SET NAME=" + StorageManager.concat("'" + copyOfPrefix + "'", "NAME", "(" + String.valueOf(copyOfNr) + ")") +
                         " WHERE ID=" + firstCreatedNodeId);
 */
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
     public void activateNode(Connection con, SequencerEngine seq, ContentEngine ce, FxTreeMode mode,
                              final long nodeId, boolean activateContents) throws FxApplicationException {
         if (mode == FxTreeMode.Live) //Live tree can not be activated!
             return;
         long ids[] = getIdChain(con, mode, nodeId); //all id's up to the root node
         acquireLocksForUpdate(con, mode, Arrays.asList(ArrayUtils.toObject(ids)));
         try {
             // lock node in live tree including all children (which *can* be removed if they were removed in the edit tree)
             acquireLocksForUpdate(con, FxTreeMode.Live, selectDirectChildNodeIds(con, FxTreeMode.Live, nodeId, true));
         } catch (SQLException e) {
             throw new FxDbException(e);
         }
 
         for (long id : ids) {
             if (id == ROOT_NODE) continue;
             FxTreeNode srcNode = getNode(con, mode, id);
             //check if the node already exists in the live tree
             if (exists(con, FxTreeMode.Live, id)) {
                 //Move and setData will not do anything if the node is already in its correct place and
                 move(con, seq, FxTreeMode.Live, id, srcNode.getParentNodeId(), srcNode.getPosition());
                 setData(con, FxTreeMode.Live, id, srcNode.getData());
             } else {
                 createNode(con, seq, ce, FxTreeMode.Live, srcNode.getId(), srcNode.getParentNodeId(),
                         srcNode.getName(), srcNode.getLabel(), srcNode.getPosition(),
                         srcNode.getReference(), srcNode.getData(), activateContents);
             }
 
             // Remove all deleted direct child nodes
             Statement stmt = null;
             Statement stmt2 = null;
             try {
                 stmt = con.createStatement();
                 stmt2 = con.createStatement();
                 stmt2.execute(StorageManager.getReferentialIntegrityChecksStatement(false));
                 try {
                     ResultSet rs = stmt.executeQuery(
                             "SELECT DISTINCT tl.ID FROM " + getTable(FxTreeMode.Live) + " tl " +
                                     "LEFT JOIN " + getTable(FxTreeMode.Edit) + " te ON tl.ID=te.ID WHERE te.ID=null AND " +
                                     "te.PARENT=" + nodeId + " AND tl.PARENT=" + nodeId);
                     while (rs != null && rs.next()) {
                         long deleteId = rs.getLong(1);
 //                        System.out.println("==> deleted:"+deleteId);
                         acquireLocksForUpdate(con, FxTreeMode.Live, Arrays.asList(deleteId));
                         stmt2.addBatch("DELETE FROM " + getTable(FxTreeMode.Live) + " WHERE ID=" + deleteId);
 
                     }
                     stmt2.addBatch("UPDATE " + getTable(FxTreeMode.Live) + " SET MODIFIED_AT=" + System.currentTimeMillis());
                     stmt2.executeBatch();
                 } finally {
                     stmt2.execute(StorageManager.getReferentialIntegrityChecksStatement(true));
                 }
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
      * {@inheritDoc}
      */
     public void activateSubtree(Connection con, SequencerEngine seq, ContentEngine ce, FxTreeMode mode,
                                 long nodeId, boolean activateContents) throws FxApplicationException {
         if (nodeId == ROOT_NODE) {
             activateAll(con, mode);
             return;
         }
 
         final FxTreeNodeInfo sourceNode = getTreeNodeInfo(con, mode, nodeId);
         final long destination = sourceNode.getParentId();
 
         // Make sure the path up to the root node is activated
         activateNode(con, seq, ce, mode, sourceNode.getParentId(), activateContents);
 
         try {
             // lock edit tree
             acquireLocksForUpdate(
                     con,
                     mode,
                     selectAllChildNodeIds(con, mode, sourceNode.getLeft(), sourceNode.getRight())
             );
             // lock live tree
             acquireLocksForUpdate(
                     con,
                     FxTreeMode.Live,
                     selectAllChildNodeIds(con, mode, sourceNode.getLeft(), sourceNode.getRight())
             );
         } catch (SQLException e) {
             throw new FxDbException(e);
         }
 
         //***************************************************************
         //* Cleanup all affected nodes
         //***************************************************************
 
         // First we clear all affected nodes in the live tree, since we will copy them from the edit tree.
         // We also need to delete all nodes that are children of the specified node in the edit tree, since they
         // were moved into our new subtree.
 
         // get node in live tree (with current bounds)
         FxTreeNodeInfo oldDestNode = null;
         try {
             oldDestNode = getTreeNodeInfo(con, FxTreeMode.Live, nodeId);
         } catch (FxNotFoundException e) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Activated node " + nodeId + " not yet present in Live tree.");
             }
         }
         Statement stmt = null;
         if (oldDestNode != null) {
             try {
                 String sql = "SELECT ID FROM " + getTable(FxTreeMode.Live) +
                        " WHERE (LFT>=" + sourceNode.getLeft() + " AND RGT<=" + sourceNode.getRight() + ") OR ID=" + nodeId;
                 stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql);
                 while (rs.next()) {
                     try {
                         removeNode(con, FxTreeMode.Live, ce, rs.getLong(1), true /* always forced true in live mode */);
                     } catch (FxNotFoundException e) {
                         // removed by previous call
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
                 boundaries[0], null, 0, null, null, depthDelta, destination, true, true);
 
 
         try {
             // Update the childcount of the new parents
             stmt = con.createStatement();
             stmt.addBatch("UPDATE " + getTable(FxTreeMode.Live) + " SET CHILDCOUNT=CHILDCOUNT+1 WHERE ID=" + destination);
             stmt.addBatch("UPDATE " + getTable(mode) + " SET DIRTY=" + StorageManager.getBooleanFalseExpression() +
                     " WHERE LFT>=" + sourceNode.getLeft() + " AND RGT<=" + sourceNode.getRight());
             stmt.executeBatch();
         } catch (SQLException exc) {
             throw new FxTreeException("ex.tree.activate.failed", nodeId, true, exc.getMessage());
         } finally {
             try {
                 if (stmt != null) stmt.close();
             } catch (Exception exc) {/*ignore*/}
         }
 
         //clear nodes that can not be activated since their content has no live step
         boolean orgNodeRemoved = false;
         PreparedStatement psRemove = null;
         PreparedStatement psFixChildCount = null;
         PreparedStatement psFlagDirty = null;
         PreparedStatement psEditBoundaries = null;
         try {
             // Update the childcount of the new parents
             stmt = con.createStatement();
             //                                                  1     2         3      4      5
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT l.ID, l.PARENT, l.LFT, l.RGT, c.ID FROM " + getTable(FxTreeMode.Live) + " l, " +
                     TBL_CONTENT + " c WHERE l.LFT>" + destinationNode.getLeft() + " AND l.RGT<" +
                     destinationNode.getRight() + " AND l.ref=c.id and c.live_ver=0 ORDER BY l.LFT DESC");
 
             while (rs != null && rs.next()) {
                 long rmNodeId = rs.getLong(1);
                 if (activateContents) {
                     FxPK reference = new FxPK(rs.getLong(5));
                     FxContent co = ce.load(reference);
                     //create a Live version
                     reference = createContentLiveVersion(ce, co);
                     LOG.info("Created new live version " + reference + " during activation of node " + rmNodeId);
                 } else {
                     System.out.println("removing node #" + rmNodeId + " and children");
                     if( rmNodeId == nodeId )
                         orgNodeRemoved = true;
                     if (psRemove == null) {
                         psRemove = con.prepareStatement("DELETE FROM " + getTable(FxTreeMode.Live) + " WHERE LFT>=? AND RGT<=?");
                     }
                     psRemove.setBigDecimal(1, rs.getBigDecimal(3));
                     psRemove.setBigDecimal(2, rs.getBigDecimal(4));
                     psRemove.execute();
                     if (psFixChildCount == null) {
                         psFixChildCount = con.prepareStatement("UPDATE " + getTable(FxTreeMode.Live) + " SET CHILDCOUNT=CHILDCOUNT-1 WHERE ID=?");
                     }
                     psFixChildCount.setLong(1, rs.getLong(2));
                     psFixChildCount.executeUpdate();
                     if (psEditBoundaries == null) {
                         psEditBoundaries = con.prepareStatement("SELECT LFT,RGT FROM " + getTable(FxTreeMode.Edit) + " WHERE ID=?");
                     }
                     psEditBoundaries.setLong(1, rmNodeId);
                     ResultSet rsBoundaries = psEditBoundaries.executeQuery();
                     if (rsBoundaries != null && rsBoundaries.next()) {
                         if (psFlagDirty == null) {
                             psFlagDirty = con.prepareStatement("UPDATE " + getTable(FxTreeMode.Edit) + " SET DIRTY=" + StorageManager.getBooleanTrueExpression() +
                                     " WHERE LFT>=? AND RGT<=?");
                         }
                         psFlagDirty.setBigDecimal(1, rsBoundaries.getBigDecimal(1));
                         psFlagDirty.setBigDecimal(2, rsBoundaries.getBigDecimal(2));
                         psFlagDirty.executeUpdate();
                     }
                 }
             }
         } catch (SQLException exc) {
             throw new FxTreeException("ex.tree.activate.failed", nodeId, true, exc.getMessage());
         } finally {
             Database.closeObjects(GenericTreeStorageSpreaded.class, stmt, psRemove, psFixChildCount, psFlagDirty);
         }
 
         // Make sure the node is at the correct position
         if (!orgNodeRemoved)
             move(con, seq, FxTreeMode.Live, sourceNode.getId(), sourceNode.getParentId(), sourceNode.getPosition());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void wipeTree(FxTreeMode mode, Statement stmt, FxPK rootPK) throws SQLException {
         DBStorage storage = StorageManager.getStorageImpl();
         stmt.execute(storage.getReferentialIntegrityChecksStatement(false));
         try {
             stmt.executeUpdate("DELETE FROM " + getTable(mode));
             stmt.executeUpdate("INSERT INTO " + getTable(mode) + " (ID,NAME,MODIFIED_AT,DIRTY,PARENT,DEPTH,CHILDCOUNT,REF,TEMPLATE,LFT,RGT) " +
                     "VALUES (" + ROOT_NODE + ",'Root'," + storage.getTimestampFunction() + "," +
                     storage.getBooleanFalseExpression() + ",NULL,1,0," + rootPK.getId() + ",NULL,1," + MAX_RIGHT + ")");
         } finally {
             stmt.executeUpdate(storage.getReferentialIntegrityChecksStatement(true));
         }
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
             final String sql;
             if (mode == FxTreeMode.Live) {
                 sql = "SELECT t.ID FROM " + getTable(mode) + " t, " + TBL_CONTENT + " c "
                         + " WHERE c.id=t.ref AND c.islive_ver=" + StorageManager.getBooleanTrueExpression();
             } else {
                 sql = "SELECT ID FROM " + getTable(mode);
             }
             ResultSet rs = stmt.executeQuery(sql);
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
 
                 // Direct child count check
                 stmt2 = con.createStatement();
                 rs2 = stmt2.executeQuery("SELECT COUNT(*) FROM " + getTable(mode) + " WHERE PARENT=" + id);
                 rs2.next();
                 int directChilds = rs2.getInt(1);
                 if (directChilds != node.getDirectChildCount())
                     throw new FxTreeException(LOG, "ex.tree.check.failed", mode, "#" + id + " invalid direct child count [" + directChilds + "!=" + node.getDirectChildCount() + "]");
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
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected boolean lockForUpdate(Connection con, String table, Iterable<Long> nodeIds) throws FxDbException {
         if (nodeIds == null || !nodeIds.iterator().hasNext()) {
             return tryLock(con, table, null);
         }
 
         // process nodes in partitions since some DBMS choke on large IN conditions
         for (List<Long> part : Iterables.partition(nodeIds, 500)) {
             if (!tryLock(con, table, part)) {
                 return false;   // deadlock detected
             }
         }
         return true;    // sucess
     }
 
     /**
      * Get the database specific "for update" clause to lock tables rows
      *
      * @return database specific "for update" clause
      */
     protected String getForUpdateClause() {
         return " FOR UPDATE";
     }
 
     private boolean tryLock(Connection con, String table, List<Long> part) throws FxDbException {
         PreparedStatement stmt = null;
         try {
             stmt = con.prepareStatement("SELECT id FROM " + table + " t "
                     + (part == null || part.isEmpty() ? ""
                     : " WHERE id IN (" + StringUtils.join(part, ',') + ")")
                     + getForUpdateClause());
             stmt.executeQuery();
 
             return true;
         } catch (SQLException e) {
             final DBStorage si = StorageManager.getStorageImpl();
             if (si.isDeadlock(e) || si.isQueryTimeout(e)) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Deadlock detected while locking tree tables.");
                 }
                 return false;
             } else {
                 throw new FxDbException(e);
             }
         } finally {
             Database.closeObjects(GenericTreeStorageSpreaded.class, null, stmt);
         }
     }
 }
