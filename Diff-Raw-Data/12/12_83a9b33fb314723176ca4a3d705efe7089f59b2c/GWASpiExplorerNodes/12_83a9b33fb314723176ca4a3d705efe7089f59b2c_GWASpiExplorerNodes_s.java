 /*
  * Copyright (C) 2013 Universitat Pompeu Fabra
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.gwaspi.model;
 
 import java.io.IOException;
 import java.util.List;
 import javax.swing.text.Position;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 import org.gwaspi.constants.cNetCDF.Defaults.OPType;
 import org.gwaspi.global.Text;
 import org.gwaspi.gui.GWASpiExplorerPanel;
 import org.gwaspi.gui.StartGWASpi;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class GWASpiExplorerNodes {
 
 	private static final Logger log
 			= LoggerFactory.getLogger(GWASpiExplorerNodes.class);
 
 	private GWASpiExplorerNodes() {
 	}
 
 	//<editor-fold defaultstate="expanded" desc="NODE DEFINITION">
 	public static class NodeElementInfo {
 
 		public static final int NODE_ID_NONE = 0;
 
 		private final int nodeId;
 		private final int parentNodeId;
 		private final String nodeType;
 		private final String nodeUniqueName;
 		/** This may contain a StudyKey, MatrixKey, OperationKey, ReportKey, ... */
 		private final Object contentKey;
 		private boolean collapsable;
 
 		public NodeElementInfo(
 				int parentNodeId,
 				int nodeId,
 				String nodeType,
 				String nodeName,
 				Object contentKey)
 		{
 			this.parentNodeId = parentNodeId;
 			this.nodeId = nodeId;
 			this.nodeType = nodeType;
 			this.nodeUniqueName = nodeName;
 			this.contentKey = contentKey;
 			this.collapsable = false;
 		}
 
 		@Override
 		public String toString() {
 			return nodeUniqueName;
 		}
 
 		public int getNodeId() {
 			return nodeId;
 		}
 
 		public int getParentNodeId() {
 			return parentNodeId;
 		}
 
 		public String getNodeType() {
 			return nodeType;
 		}
 
 		public String getNodeUniqueName() {
 			return nodeUniqueName;
 		}
 
 		public Object getContentKey() {
 			return contentKey;
 		}
 
 		public boolean isCollapsable() {
 			return collapsable;
 		}
 
 		public void setCollapsable(boolean collapsable) {
 			this.collapsable = collapsable;
 		}
 	}
 
 	public static class UncollapsableNodeElementInfo extends NodeElementInfo {
 
 		public UncollapsableNodeElementInfo(
 				int parentNodeId,
 				int nodeId,
 				String nodeType,
 				String nodeName,
 				Object contentKey)
 		{
 			super(parentNodeId, nodeId, nodeType, nodeName, contentKey);
 		}
 
 		@Override
 		public boolean isCollapsable() {
 			return false;
 		}
 	}
 
 	protected static DefaultMutableTreeNode createStudyTreeNode(Study study) {
 //		parentNodeId
 //		nodeId
 //		nodeType
 //		nodeUniqueName => will be rsult of toString() call of DefaultMutableTreeNode
 //		friendlyName
 
 		DefaultMutableTreeNode tn = new DefaultMutableTreeNode(
 				new NodeElementInfo(
 				NodeElementInfo.NODE_ID_NONE,
 				study.getId(),
 				Text.App.treeStudy,
 				"SID: " + study.getId() + " - " + study.getName(),
 				StudyKey.valueOf(study)));
 
 		return tn;
 	}
 
 	protected static DefaultMutableTreeNode createMatrixTreeNode(MatrixKey matrixKey) {
 		DefaultMutableTreeNode tn = null;
 		try {
 			MatrixMetadata matrixMetadata = MatricesList.getMatrixMetadataById(matrixKey);
 			tn = new DefaultMutableTreeNode(new NodeElementInfo(
 					matrixMetadata.getStudyId(),
 					matrixKey.getMatrixId(),
 					Text.App.treeMatrix,
 					"MX: " + matrixKey.getMatrixId() + " - " + matrixMetadata.getMatrixFriendlyName(),
 					matrixKey));
 		} catch (IOException ex) {
 			log.error(null, ex);
 		}
 		return tn;
 	}
 
 	protected static DefaultMutableTreeNode createSampleInfoTreeNode(StudyKey studyKey) throws IOException {
 		DefaultMutableTreeNode tn = null;
 		// CHECK IF STUDY EXISTS
 		List<SampleInfo> sampleInfos = SampleInfoList.getAllSampleInfoFromDBByPoolID(studyKey);
 		if (!sampleInfos.isEmpty()) {
 			tn = new DefaultMutableTreeNode(new NodeElementInfo(
 					studyKey.getId(), // parentNodeId
 					studyKey.getId(), // nodeId
 					Text.App.treeSampleInfo, // nodeType
 					Text.App.treeSampleInfo,
 					studyKey)); // nodeUniqueName
 		}
 		return tn;
 	}
 
 	protected static DefaultMutableTreeNode createOperationTreeNode(OperationKey operationKey) {
 		DefaultMutableTreeNode tn = null;
 		try {
 			OperationMetadata op = OperationsList.getById(operationKey.getId());
 			tn = new DefaultMutableTreeNode(new NodeElementInfo(
 					op.getParentMatrixId(),
 					operationKey.getId(),
 					Text.App.treeOperation,
 					"OP: " + operationKey.getId() + " - " + op.getFriendlyName(),
 					operationKey));
 		} catch (IOException ex) {
 			log.error(null, ex);
 		}
 		return tn;
 	}
 
 	protected static DefaultMutableTreeNode createSubOperationTreeNode(OperationKey operationKey) {
 		DefaultMutableTreeNode tn = null;
 		try {
 
 //			parentNodeId
 //			nodeId
 //			pathNodeIds
 //			nodeType
 //			studyNodeName
 //			nodeUniqueName
 
 			OperationMetadata op = OperationsList.getById(operationKey.getId());
 //			int[] pathIds = new int[]{0, op.getId(), op.getParentMatrixId(), op.getParentOperationId(), opId};
 			tn = new DefaultMutableTreeNode(new NodeElementInfo(
 					op.getParentOperationId(),
 					operationKey.getId(),
 					Text.App.treeOperation,
 					"OP: " + operationKey.getId() + " - " + op.getFriendlyName(),
 					operationKey));
 		} catch (IOException ex) {
 			log.error(null, ex);
 		}
 		return tn;
 	}
 
 	protected static DefaultMutableTreeNode createReportTreeNode(ReportKey reportKey) {
 		DefaultMutableTreeNode tn = null;
 		try {
 			Report rp = ReportsList.getReport(reportKey);
 //			int[] pathIds = new int[]{0, rp.getId(), rp.getParentMatrixId(), rp.getParentOperationId(), rpId};
 			tn = new DefaultMutableTreeNode(new NodeElementInfo(
 					rp.getParentMatrixId(),
 					reportKey.getId(),
 					Text.App.treeReport,
 					"RP: " + reportKey.getId() + " - " + rp.getFriendlyName(),
 					reportKey));
 		} catch (IOException ex) {
 			log.error(null, ex);
 		}
 		return tn;
 	}
 	//</editor-fold>
 
 	//<editor-fold defaultstate="expanded" desc="NODE MANAGEMENT">
 	//<editor-fold defaultstate="expanded" desc="STUDY NODES">
 	public static void insertLatestStudyNode() throws IOException {
 		try {
 			// GET LATEST ADDED STUDY
 			List<Study> studyList = StudyList.getStudyList();
 			TreePath parentPath = GWASpiExplorerPanel.getSingleton().getTree().getNextMatch(Text.App.treeStudyManagement, 0, Position.Bias.Forward);
 
 			DefaultMutableTreeNode newNode = createStudyTreeNode(studyList.get(studyList.size() - 1));
 
 			if (parentPath != null) {
 				DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
 				addNode(parentNode, newNode, true);
 			}
 		} catch (IOException ex) {
 			log.error(null, ex);
 		}
 	}
 
 	public static void insertStudyNode(StudyKey studyKey) throws IOException {
 		try {
 			// GET STUDY
 			TreePath parentPath = GWASpiExplorerPanel.getSingleton().getTree().getNextMatch(Text.App.treeStudyManagement, 0, Position.Bias.Forward);
 
 			Study study = StudyList.getStudy(studyKey);
 			DefaultMutableTreeNode newNode = createStudyTreeNode(study);
 
 			if (parentPath != null) {
 				DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
 				addNode(parentNode, newNode, true);
 			}
 		} catch (IOException ex) {
 			log.error(null, ex);
 		}
 	}
 
 	public static void deleteStudyNode(StudyKey studyKey) {
 		try {
 			// GET DELETE PATH BY PREFIX ONLY
 			TreePath deletePath = GWASpiExplorerPanel.getSingleton().getTree().getNextMatch("SID: " + studyKey.getId() + " - ", 0, Position.Bias.Forward);
 
 			if (deletePath != null) {
 				DefaultMutableTreeNode deleteNode = (DefaultMutableTreeNode) deletePath.getLastPathComponent();
 				deleteNode(deleteNode);
 			}
 		} catch (Exception ex) {
 			log.error(null, ex);
 		}
 	}
 	//</editor-fold>
 
 	//<editor-fold defaultstate="expanded" desc="MATRIX NODES">
 	public static void insertMatrixNode(MatrixKey matrixKey) throws IOException {
 		if (StartGWASpi.guiMode) {
 			try {
 				// GET STUDY
 				Study study = StudyList.getStudy(matrixKey.getStudyKey());
 				TreePath parentPath = GWASpiExplorerPanel.getSingleton().getTree().getNextMatch("SID: " + study.getId() + " - " + study.getName(), 0, Position.Bias.Forward);
 
 				DefaultMutableTreeNode newNode = createMatrixTreeNode(matrixKey);
 
 				if (parentPath != null) {
 					DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
 					addNode(parentNode, newNode, true);
 				}
 			} catch (IOException ex) {
 				log.error(null, ex);
 			}
 		}
 	}
 
 	public static void deleteMatrixNode(MatrixKey matrixKey) {
 		try {
 			// GET DELETE PATH BY PREFIX ONLY
 			TreePath deletePath = GWASpiExplorerPanel.getSingleton().getTree().getNextMatch("MX: " + matrixKey.getMatrixId() + " - ", 0, Position.Bias.Forward);
 
 			if (deletePath != null) {
 				DefaultMutableTreeNode deleteNode = (DefaultMutableTreeNode) deletePath.getLastPathComponent();
 				deleteNode(deleteNode);
 			}
 		} catch (Exception ex) {
 			log.error(null, ex);
 		}
 	}
 	//</editor-fold>
 
 	//<editor-fold defaultstate="expanded" desc="OPERATION NODES">
 	public static void insertOperationUnderMatrixNode(OperationKey operationKey) throws IOException {
 		try {
 
 			// GET MATRIX
 			MatrixMetadata matrixMetadata = MatricesList.getMatrixMetadataById(operationKey.getParentMatrixKey());
 			TreePath parentPath = GWASpiExplorerPanel.getSingleton().getTree().getNextMatch("MX: " + operationKey.getParentMatrixId() + " - " + matrixMetadata.getMatrixFriendlyName(), 0, Position.Bias.Forward);
 
 			DefaultMutableTreeNode newNode = createOperationTreeNode(operationKey);
 
 			if (parentPath != null) {
 				DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
 				addNode(parentNode, newNode, true);
 			}
 		} catch (IOException ex) {
 			log.error(null, ex);
 		}
 	}
 
 	public static void insertSubOperationUnderOperationNode(OperationKey parentOpKey, OperationKey operationKey) throws IOException {
 		try {
 			// GET MATRIX
 			OperationMetadata parentOP = OperationsList.getOperation(parentOpKey);
			TreePath parentPath = GWASpiExplorerPanel.getSingleton().getTree().getNextMatch("OP: " + parentOpKey + " - " + parentOP.getFriendlyName(), 0, Position.Bias.Forward);
 
 			DefaultMutableTreeNode newNode = createOperationTreeNode(operationKey);
 
 			if (parentPath != null) {
 				DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
 				addNode(parentNode, newNode, true);
 			}
 		} catch (IOException ex) {
 			log.error(null, ex);
 		}
 	}
 
 	public static void deleteOperationNode(OperationKey operationKey) {
 		try {
 			// GET DELETE PATH BY PREFIX ONLY
 			TreePath deletePath = GWASpiExplorerPanel.getSingleton().getTree().getNextMatch("OP: " + operationKey.getId() + " - ", 0, Position.Bias.Forward);
 
 			if (deletePath != null) {
 				DefaultMutableTreeNode deleteNode = (DefaultMutableTreeNode) deletePath.getLastPathComponent();
 				deleteNode(deleteNode);
 			}
 		} catch (Exception ex) {
 			log.error(null, ex);
 		}
 	}
 	//</editor-fold>
 
 	//<editor-fold defaultstate="expanded" desc="REPORT NODES">
 	public static void insertReportsUnderOperationNode(OperationKey parentOpKey) throws IOException {
 		if (StartGWASpi.guiMode) {
 			try {
 				// GET OPERATION
 				OperationMetadata parentOP = OperationsList.getOperation(parentOpKey);
				TreePath parentPath = GWASpiExplorerPanel.getSingleton().getTree().getNextMatch("OP: " + parentOpKey + " - " + parentOP.getFriendlyName(), 0, Position.Bias.Forward);
 				DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
 
 				// GET ALL REPORTS UNDER THIS OPERATION
 				List<Report> reportsList = ReportsList.getReportsList(parentOpKey.getId(), Integer.MIN_VALUE);
 				for (int n = 0; n < reportsList.size(); n++) {
 					Report rp = reportsList.get(n);
 
 					if (!parentOP.getOperationType().equals(OPType.HARDY_WEINBERG) // DON'T SHOW SUPERFLUOUS OPEARATION INFO
 							&& !parentOP.getOperationType().equals(OPType.SAMPLE_QA)
 							&& !rp.getReportType().equals(OPType.ALLELICTEST))
 					{
 						DefaultMutableTreeNode newNode = createReportTreeNode(ReportKey.valueOf(reportsList.get(n)));
 						addNode(parentNode, newNode, true);
 					}
 				}
 			} catch (IOException ex) {
 				log.error(null, ex);
 			}
 		}
 	}
 	//</editor-fold>
 
 	public static DefaultMutableTreeNode addNode(
 			DefaultMutableTreeNode parentNode,
 			DefaultMutableTreeNode child,
 			boolean shouldBeVisible)
 	{
 		DefaultTreeModel treeModel = (DefaultTreeModel) GWASpiExplorerPanel.getSingleton().getTree().getModel();
 		treeModel.insertNodeInto(child, parentNode, parentNode.getChildCount());
 
 		GWASpiExplorerPanel.getSingleton().getTree().expandPath(new TreePath(parentNode.getPath()));
 
 		return child;
 	}
 
 	public static DefaultMutableTreeNode deleteNode(DefaultMutableTreeNode child) {
 
 		DefaultTreeModel treeModel = (DefaultTreeModel) GWASpiExplorerPanel.getSingleton().getTree().getModel();
 		treeModel.removeNodeFromParent(child);
 
 		return child;
 	}
 	//</editor-fold>
 }
