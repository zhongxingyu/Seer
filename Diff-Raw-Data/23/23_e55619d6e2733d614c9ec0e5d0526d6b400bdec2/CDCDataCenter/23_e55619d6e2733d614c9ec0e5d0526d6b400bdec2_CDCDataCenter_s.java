 package edu.pdx.svl.coDoc.cdc.datacenter;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.Stack;
 import java.util.Vector;
 
 import org.eclipse.cdt.core.model.ICElement;
 import org.eclipse.cdt.core.model.ITranslationUnit;
 import org.eclipse.cdt.internal.ui.editor.CEditor;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 import org.simpleframework.xml.Serializer;
 import org.simpleframework.xml.core.Persister;
 
 import edu.pdx.svl.coDoc.cdc.editor.CDCEditor;
 import edu.pdx.svl.coDoc.cdc.editor.EntryEditor;
 import edu.pdx.svl.coDoc.cdc.editor.EntryEditorInput;
 import edu.pdx.svl.coDoc.cdc.editor.MyASTTree;
 
 
 public class CDCDataCenter {
 	private static CDCDataCenter instance = null;	
 	private Hashtable<String, CDCCachedFile> projects;
 	// eclipse TreeViewer has a bug, use invisible root as a workaround.
 	private EntryNode invisibleroot = new EntryNode(null);
 
 	public static CDCDataCenter getInstance() {
 		if(instance == null) {
 			instance = new CDCDataCenter();
 		}
 		return instance;
 	}
 	
 	CDCDataCenter() {
 		projects = new Hashtable<String, CDCCachedFile>();
 	}
 	
 	private CDCCachedFile getCDCCachedFile(String cdcfilename) {
 		if(cdcfilename==null) {
 			return null;
 		}
 		if(projects.get(cdcfilename)==null) {
 			CDCCachedFile cdcCachedFile = new CDCCachedFile();
 			File tempFile = new File(cdcfilename);
 			if(tempFile.exists()) {
 				cdcCachedFile.open(cdcfilename);
 				projects.put(cdcfilename, cdcCachedFile);
 			}
 		}
 		return projects.get(cdcfilename);
 	}
 	
 	public void refresh(String cdcfilename) {
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		if(f!=null) {
 			f.refresh();
 		}
 	}
 	public void flush(String cdcfilename) {
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		if(f!=null) {
 			f.flush();
 		}
 	}
 	
 	public void addCodeFileEntry(String cdcfilename, String filename) {
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		if(f!=null) {
 			f.addCodeFileEntry(filename);
 		}
 	}
 	
 	public void deleteCodeFileEntry(String cdcfilename, String uuid) {
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		if(f!=null) {
 			f.deleteCodeFileEntry(uuid);
 		}
 	}
 	
 	public void addSpecFileEntry(String cdcfilename, String filename) {
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		if(f!=null) {
 			f.addSpecFileEntry(filename);
 		}
 	}
 	
 	public void deleteSpecFileEntry(String cdcfilename, String uuid) {
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		if(f!=null) {
 			f.deleteSpecFileEntry(uuid);
 		}
 	}
 	
 	public String addCategoryEntry(String cdcfilename, String parentfolderuuid, String foldername) {
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		if(f!=null) {
 			return f.addFolderEntry(parentfolderuuid, foldername);
 		} else {
 			return null;
 		}
 	}
 	
 	public CategoryEntry getCategoryEntry(String cdcfilename, String uuid) {
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		FolderEntry entry = null;
 		if(f!=null) {
 			entry = f.getFolderEntry(uuid);
 		}
 		if(entry != null) {
 			return new CategoryEntry(uuid, entry.getTime(), entry.getOS(), ((FolderEntry) entry).getFoldername(), entry.getCreater(), entry.getFolderpath());
 		}
 		return null;
 	}
 	
 	public void deleteCategoryEntry(String cdcfilename, String uuid) {
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		if(f!=null) {
 			f.deleteFolderEntry(uuid);
 		}
 	}
 	
 	/*public void addLinkEntry(String cdcfilename, String codefilename, CodeSelection codeselpath, String specfilename, SpecSelection specselpath, String comment) {
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		String parentfolderuuid = f.getMapIdTree().getData();
 		if(f!=null) {
 			f.addMapEntry(parentfolderuuid, codefilename, codeselpath, specfilename, specselpath, comment);
 		}
 	}*/
 	
 	public String addLinkEntry(String cdcfilename, String parentfolderuuid, String codefilename, CodeSelection codeselpath, String specfilename, SpecSelection specselpath, String comment) {
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		if(f!=null) {
 			return f.addMapEntry(parentfolderuuid, codefilename, codeselpath, specfilename, specselpath, comment);
 		} else {
 			return null;
 		}
 	}
 	
 	public String editLinkEntry(String cdcfilename, String uuid, String codefilename, CodeSelection codeselpath, String specfilename, SpecSelection specselpath, String comment) {
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		if(f!=null) {
 			return f.editMapEntry(uuid, codefilename, codeselpath, specfilename, specselpath, comment);
 		} else {
 			return null;
 		}
 	}
 	
 	public LinkEntry getLinkEntry(String cdcfilename, String uuid) {
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		MapEntry entry = null;
 		if(f!=null) {
 			entry = f.getMapEntry(uuid);
 		}
 		if(entry != null) {
 			return new LinkEntry(uuid, entry.getTime(), entry.getOS(), entry.getCreater(), f.getCodeFilename(entry.getCodefileUUID()), entry.getCodeselpath(), f.getSpecFilename(entry.getSpecfileUUID()), entry.getSpecselpath(), entry.getComment());
 		}
 		return null;
 	}
 	
 	public void deleteLinkEntry(String cdcfilename, String uuid) {
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		if(f!=null) {
 			f.deleteMapEntry(uuid);
 		}
 	}
 	
 	public boolean parentOf(String cdcfilename, String childuuid, String parentuuid) {
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		if(f!=null) {
 			return f.parentOf(childuuid, parentuuid);
 		}
 		return false;
 	}
 	
 	public void moveEntry(String cdcfilename, String sourceuuid, String destuuid) {
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		if(f!=null) {
 			f.moveEntry(sourceuuid, destuuid);
 		}
 	}
 	
 	public void setLastOpenedCodeFilename(String cdcfilename, String codeFilename) {
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		if(f!=null) {
 			f.setLastOpenedCodeFilename(codeFilename);
 		}
 	}
 	public String getLastOpenedCodeFilename(String cdcfilename) {
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		if(f!=null) {
 			return f.getLastOpenedCodeFilename();
 		} else {
 			return null;
 		}
 	}
 	
 	public void setLastOpenedSpecFilename(String cdcfilename, String specFilename) {
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		if(f!=null) {
 			f.setLastOpenedSpecFilename(specFilename);
 		}
 	}
 	public String getLastOpenedSpecFilename(String cdcfilename) {
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		if(f!=null) {
 			return f.getLastOpenedSpecFilename();
 		} else {
 			return null;
 		}
 	}
 	
 	private boolean isEntryMatch(LinkEntry entry, MapSelectionFilter filter) {
 		boolean res = false;
 		switch(filter.getSelector()) {
 		case MapSelectionFilter.ALLDATA:
 			if((entry.codefilename.contains(filter.getSearchStr()))
 				|| (entry.codeselpath.getSyntaxCodeText().contains(filter.getSearchStr()))
 				|| (entry.specfilename.contains(filter.getSearchStr()))
 				|| (entry.specselpath.getPage(new String()).equals(filter.getSearchStr()))
 				|| (entry.specselpath.getPDFText(new String()).contains(filter.getSearchStr()))
 				|| (entry.comment.contains(filter.getSearchStr()))) {
 				res = true;
 			}
 			break;
 		case MapSelectionFilter.CODEFILE:
 			if(entry.codefilename.contains(filter.getSearchStr())) {
 				res = true;
 			}
 			break;
 		case MapSelectionFilter.CODETEXT:
 			if(entry.codeselpath.getSyntaxCodeText().contains(filter.getSearchStr())) {
 				res = true;
 			}
 			break;
 		case MapSelectionFilter.SPECFILE:
 			if(entry.specfilename.contains(filter.getSearchStr())) {
 				res = true;
 			}
 			break;
 		case MapSelectionFilter.PDFPAGE:
 			if(entry.specselpath.getPage(new String()).equals(filter.getSearchStr())) {
 				res = true;
 			}
 			break;
 		case MapSelectionFilter.SPECTEXT:
 			if(entry.specselpath.getPDFText(new String()).contains(filter.getSearchStr())) {
 				res = true;
 			}
 			break;
 		case MapSelectionFilter.COMMENT:
 			if(entry.comment.contains(filter.getSearchStr())) {
 				res = true;
 			}
 			break;
 		}
 		return res;
 	}
 	/**
 	 * 
 	 * @param cdcfilename
 	 * @param filter
 	 * @return The list of uuids of maps.
 	 */
 	public EntryNode getLinkTable(String cdcfilename, MapSelectionFilter filter) {
 		invisibleroot.clearChildren();
 		if(cdcfilename==null) {
 			return invisibleroot;
 		}
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		Stack<FolderMapTreeNode> stack = new Stack<FolderMapTreeNode>();
 		stack.push(f.getMapIdTree());
 		while(!stack.empty()) {
 			FolderMapTreeNode node = stack.pop();
 			String uuid = node.getData();
 			if(getCategoryEntry(cdcfilename, uuid) != null) {
 				// this is a folder
 				FolderMapTreeNode[] children = node.getChildren();
 				for(int i=children.length-1; i>=0; i--) {
 					stack.push(children[i]);
 				}
 			} else {
 				// this is a map
 				MapEntry entry = f.getMapEntry(uuid);
 				LinkEntry le = new LinkEntry(uuid, entry.getTime(), entry.getOS(), entry.getCreater(), f.getCodeFilename(entry.getCodefileUUID()), entry.getCodeselpath(), f.getSpecFilename(entry.getSpecfileUUID()), entry.getSpecselpath(), entry.getComment());
 				if(isEntryMatch(le, filter)) {
 					EntryNode en = new EntryNode(le);
 					invisibleroot.addChildZ(en);
 					en.setParent(invisibleroot);					
 				}
 			}
 		}
 		return invisibleroot;
 	}
 	public EntryNode sortLinkTable(String cdcfilename, MapSelectionSort sorter) {
 		sortTree(invisibleroot,sorter);
 		return invisibleroot;
 	}
 	
 	public EntryNode getCategoryTree(String cdcfilename) {
 		invisibleroot.clearChildren();
 		if(cdcfilename==null) {
 			return null;
 		}
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		Stack<FolderMapTreeNode> stack = new Stack<FolderMapTreeNode>();
 		stack.push(f.getMapIdTree());
 		Stack<EntryNode> stackdes = new Stack<EntryNode>();
 		String uuid = f.getMapIdTree().getData();
 		FolderMapEntry entry = f.getFolderEntry(uuid);
 		EntryNode childdes = new EntryNode(new CategoryEntry(uuid, entry.getTime(), entry.getOS(), entry.getCreater(), ((FolderEntry) entry).getFoldername(), ((FolderEntry) entry).getFolderpath()));
 		stackdes.push(childdes);
 		invisibleroot.addChildA(childdes);
 		childdes.setParent(invisibleroot);
 		while(!stack.empty()) {
 			FolderMapTreeNode node = stack.pop();
 			EntryNode parentdes = stackdes.pop();
 			uuid = node.getData();
 			if(getCategoryEntry(cdcfilename, uuid) != null) {
 				// this is a folder
 				FolderMapTreeNode[] children = node.getChildren();
 				for(int i=children.length-1; i>=0; i--) {
 					stack.push(children[i]);
 					uuid = children[i].getData();
 					entry = f.getFolderEntry(uuid);
 					if(entry != null) {
 						childdes = new EntryNode(new CategoryEntry(uuid, entry.getTime(), entry.getOS(), entry.getCreater(), ((FolderEntry) entry).getFoldername(), ((FolderEntry) entry).getFolderpath()));
 						parentdes.addChildA(childdes);
 						childdes.setParent(parentdes);
 					} else {
 						entry = f.getMapEntry(uuid);
 						LinkEntry le = new LinkEntry(uuid, entry.getTime(), entry.getOS(), entry.getCreater(), f.getCodeFilename(((MapEntry) entry).getCodefileUUID()), ((MapEntry) entry).getCodeselpath(), f.getSpecFilename(((MapEntry) entry).getSpecfileUUID()), ((MapEntry) entry).getSpecselpath(), ((MapEntry) entry).getComment());
 						childdes = new EntryNode(le);
 					}
 					stackdes.push(childdes);
 				}
 			}
 		}
 		return invisibleroot;
 	}
 	public EntryNode getLinkTree(String cdcfilename, MapSelectionFilter filter) {
 		invisibleroot.clearChildren();
 		if(cdcfilename==null) {
 			return null;
 		}
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		Stack<FolderMapTreeNode> stack = new Stack<FolderMapTreeNode>();
 		stack.push(f.getMapIdTree());
 		Stack<EntryNode> stackdes = new Stack<EntryNode>();
 		String uuid = f.getMapIdTree().getData();
 		FolderMapEntry entry = f.getFolderEntry(uuid);
 		EntryNode childdes = new EntryNode(new CategoryEntry(uuid, entry.getTime(), entry.getOS(), entry.getCreater(), ((FolderEntry) entry).getFoldername(), ((FolderEntry) entry).getFolderpath()));
 		stackdes.push(childdes);
 		invisibleroot.addChildA(childdes);
 		childdes.setParent(invisibleroot);
 		while(!stack.empty()) {
 			FolderMapTreeNode node = stack.pop();
 			EntryNode parentdes = stackdes.pop();
 			uuid = node.getData();
 			if(getCategoryEntry(cdcfilename, uuid) != null) {
 				// this is a folder
 				FolderMapTreeNode[] children = node.getChildren();
 				for(int i=children.length-1; i>=0; i--) {
 					stack.push(children[i]);
 					uuid = children[i].getData();
 					entry = f.getFolderEntry(uuid);
 					if(entry != null) {
 						childdes = new EntryNode(new CategoryEntry(uuid, entry.getTime(), entry.getOS(), entry.getCreater(), ((FolderEntry) entry).getFoldername(), ((FolderEntry) entry).getFolderpath()));
 						parentdes.addChildA(childdes);
 						childdes.setParent(parentdes);
 					} else {
 						entry = f.getMapEntry(uuid);
 						LinkEntry le = new LinkEntry(uuid, entry.getTime(), entry.getOS(), entry.getCreater(), f.getCodeFilename(((MapEntry) entry).getCodefileUUID()), ((MapEntry) entry).getCodeselpath(), f.getSpecFilename(((MapEntry) entry).getSpecfileUUID()), ((MapEntry) entry).getSpecselpath(), ((MapEntry) entry).getComment());
 						childdes = new EntryNode(le);
 						if(isEntryMatch(le, filter)) {
 							parentdes.addChildA(childdes);
 							childdes.setParent(parentdes);
 						}
 					}
 					stackdes.push(childdes);
 				}
 			}
 		}
 		return invisibleroot;
 	}
 	public EntryNode getEntryNode(String cdcfilename, String uuid) {
 		if((invisibleroot==null) || (!invisibleroot.hasChildren())) {
 			return null;
 		}
 		Stack<EntryNode> stack = new Stack<EntryNode>();
 		for(EntryNode n : invisibleroot.getChildren()) {
 			stack.push(n);
 		}
 		while(!stack.empty()) {
 			EntryNode node = stack.pop();
 			if(((BaseEntry)node.getData()).uuid.equals(uuid)) {
 				return node;
 			}
 			if(node.hasChildren()) {
 				for(EntryNode n : node.getChildren()) {
 					stack.push(n);
 				}
 			}
 		}
 		return null;
 	}
 	public EntryNode sortLinkTree(String cdcfilename, MapSelectionSort sorter) {
 		sortTree(invisibleroot,sorter);
 		return invisibleroot;
 	}
 	public String getPreviousNodeId(String cdcfilename, String uuid) {
 		if((invisibleroot==null) || (!invisibleroot.hasChildren())) {
 			return null;
 		}
 		String preuuid = null;
 		Stack<EntryNode> stack = new Stack<EntryNode>();
		for(EntryNode n : invisibleroot.getChildren()) {
			stack.push(n);
 		}
 		while(!stack.empty()) {
 			EntryNode node = stack.pop();
 			if(((BaseEntry)node.getData()).uuid.equals(uuid)) {
 				return preuuid;
 			}
 			if(node.hasChildren()) {
				for(EntryNode n : node.getChildren()) {
					stack.push(n);
 				}
 			}
 			preuuid = ((BaseEntry)node.getData()).uuid;
 		}
 		return null;
 	}
 	public String getNextNodeId(String cdcfilename, String uuid) {
 		if((invisibleroot==null) || (!invisibleroot.hasChildren())) {
 			return null;
 		}
 		Stack<EntryNode> stack = new Stack<EntryNode>();
		for(EntryNode n : invisibleroot.getChildren()) {
			stack.push(n);
 		}
 		while(!stack.empty()) {
 			EntryNode node = stack.pop();
 			if(((BaseEntry)node.getData()).uuid.equals(uuid)) {
 				if(!stack.empty()) {
 					node = stack.pop();
 					return ((BaseEntry)node.getData()).uuid;
 				} else {
 					return null;
 				}
 			}
 			if(node.hasChildren()) {
				for(EntryNode n : node.getChildren()) {
					stack.push(n);
 				}
 			}
 		}
 		return null;
 	}
 	private void sortTree(EntryNode root, MapSelectionSort sorter) {
 		Stack<EntryNode> stack = new Stack<EntryNode>();
 		stack.push(root);
 		while(!stack.empty()) {
 			EntryNode parent = stack.pop();
 			if(parent.hasChildren()) {
 				EntryNode[] children = parent.getChildren();
 				for(int i=children.length-1; i>0; i--) {
 					for(int j=0; j<i; j++) {
 						boolean needSwap = false;
 						switch(sorter.getKey()) {
 						case MapSelectionSort.CODEFILE:
 							needSwap = less(children[j],children[j+1],sorter) ^ sorter.getCodefilename();
 							break;
 						case MapSelectionSort.CODETEXT:
 							needSwap = less(children[j],children[j+1],sorter) ^ sorter.getCodefiletext();
 							break;
 						case MapSelectionSort.SPECFILE:
 							needSwap = less(children[j],children[j+1],sorter) ^ sorter.getSpecfilename();
 							break;
 						case MapSelectionSort.PDFPAGE:
 							needSwap = less(children[j],children[j+1],sorter) ^ sorter.getSpecfilepage();
 							break;
 						case MapSelectionSort.SPECTEXT:
 							needSwap = less(children[j],children[j+1],sorter) ^ sorter.getSpecfiletext();
 							break;
 						case MapSelectionSort.COMMENT:
 							needSwap = less(children[j],children[j+1],sorter) ^ sorter.getComments();
 							break;
 						}
 						if((children[j].getData() instanceof CategoryEntry) && (children[j+1].getData() instanceof LinkEntry)) {
 							needSwap = false;
 						} else if((children[j].getData() instanceof LinkEntry) && (children[j+1].getData() instanceof CategoryEntry)) {
 							needSwap = true;
 						}
 						if(needSwap) {
 							EntryNode temp = children[j];
 							children[j] = children[j+1];
 							children[j+1] = temp;
 						}
 					}
 				}
 				parent.clearChildren();
 				for(int i=0;i<children.length;i++) {
 					parent.addChildZ(children[i]);
 					stack.push(children[i]);
 				}
 			}
 		}
 	}
 	private boolean less(EntryNode node1, EntryNode node2, MapSelectionSort sorter) {
 		if((node1.getData() instanceof CategoryEntry) && (node2.getData() instanceof LinkEntry)) {
 			return true;
 		} else if((node1.getData() instanceof LinkEntry) && (node2.getData() instanceof CategoryEntry)) {
 			return false;
 		} else if((node1.getData() instanceof CategoryEntry) && (node2.getData() instanceof CategoryEntry)) {
 			CategoryEntry e1 = (CategoryEntry) node1.getData();
 			CategoryEntry e2 = (CategoryEntry) node2.getData();
 			if(e1.name.compareToIgnoreCase(e2.name) > 0) {
 				return false;
 			} else {
 				return true;
 			}
 		} else {
 			LinkEntry e1 = (LinkEntry) node1.getData();
 			LinkEntry e2 = (LinkEntry) node2.getData();
 			if(sorter.isNeedsort()) {
 				switch(sorter.getKey()) {
 				case MapSelectionSort.CODEFILE:
 					if(e1.codefilename.compareToIgnoreCase(e2.codefilename) > 0) {
 						return false;
 					} else {
 						return true;
 					}
 				case MapSelectionSort.CODETEXT:
 					if(e1.codeselpath.getSyntaxCodeText().compareToIgnoreCase(e2.codeselpath.getSyntaxCodeText()) > 0) {
 						return false;
 					} else {
 						return true;
 					}
 				case MapSelectionSort.SPECFILE:
 					if(e1.specfilename.compareToIgnoreCase(e2.specfilename) > 0) {
 						return false;
 					} else {
 						return true;
 					}
 				case MapSelectionSort.PDFPAGE:
 					//if(e1.specselpath.getPage() > e2.specselpath.getPage()) {
 					if(e1.specselpath.getPage(new String()).compareToIgnoreCase(e2.specselpath.getPage(new String())) > 0) {
 						return false;
 					} else {
 						return true;
 					}
 				case MapSelectionSort.SPECTEXT:
 					if(e1.specselpath.getPDFText(new String()).compareToIgnoreCase(e2.specselpath.getPDFText(new String())) > 0) {
 						return false;
 					} else {
 						return true;
 					}
 				case MapSelectionSort.COMMENT:
 					if(e1.comment.compareToIgnoreCase(e2.comment) > 0) {
 						return false;
 					} else {
 						return true;
 					}
 				}
 			}
 		}
 		return true;
 	}
 	
 	CDCModel getCDCModel(String cdcfilename) {
 		CDCCachedFile f = getCDCCachedFile(cdcfilename);
 		if(f!=null) {
 			return f.getCDCModel();
 		}
 		return null;
 	}
 	
 	public static void convertCDCFile0() {
 		CDCModelV1 cdcModel1 = null;
 		File cdcFile1 = new File("/home/derek/runtime-EclipseApplication/dio/dio.raw.cdc");
 		Serializer serializer = new Persister();
 		try {
 			cdcModel1 = serializer.read(CDCModelV1.class, cdcFile1);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		CDCModelV2 cdcModel2 = new CDCModelV2();
 		cdcModel2.getHead().setCreatetime(cdcModel1.getHead().getCreatetime());
 		cdcModel2.getHead().setOS(cdcModel1.getHead().getOS());
 		cdcModel2.getHead().setCreater(cdcModel1.getHead().getCreater());
 		cdcModel2.getHead().setFiletype(cdcModel1.getHead().getFiletype());
 		cdcModel2.setLastOpenedCodeFilename(cdcModel1.getLastOpenedCodeFilename());
 		cdcModel2.setLastOpenedSpecFilename(cdcModel1.getLastOpenedSpecFilename());
 		Vector<MapEntryV1> maps1 = cdcModel1.getMapEntries();
 		Vector<OpEntryV1> ops1 = cdcModel1.getOperationList();
 		Iterator it1 = maps1.iterator();
 		while(it1.hasNext()) {
 			MapEntryV1 entry1 = (MapEntryV1) it1.next();
 			String codefilename1 = entry1.getCodefilename();
 			CodeSelectionV1 codeselpath1 = entry1.getCodeselpath();
 			String specfilename1 = entry1.getSpecfilename();
 			SpecSelectionV1 specselpath1 = entry1.getSpecselpath();
 			String comment1 = entry1.getComment();
 			
 			FolderMapTreeNodeV2 root = cdcModel2.getMapIdTree();
 			String parentfolderuuid = root.getData();
 			String codefilename2 = codefilename1;
 			CodeSelectionV2 codeselpath2 = new CodeSelectionV2();
 			codeselpath2.setSelCodePath(codeselpath1.getCodeSelPath());
 			codeselpath2.setSelCodeText(codeselpath1.getCodeText());
 			String specfilename2 = specfilename1;
 			SpecSelectionV2 specselpath2 = new SpecSelectionV2();
 			specselpath2.setLeft(specselpath1.getLeft());
 			specselpath2.setRight(specselpath1.getRight());
 			specselpath2.setTop(specselpath1.getTop());
 			specselpath2.setBottom(specselpath1.getBottom());
 			specselpath2.setPage(specselpath1.getPage());
 			specselpath2.setPDFText(specselpath1.getPDFText());
 			String comment2 = comment1;
 			cdcModel2.addMapEntry(parentfolderuuid, codefilename2, codeselpath2, specfilename2, specselpath2, comment2);
 		}
 		File cdcFile2 = new File("/home/derek/runtime-EclipseApplication/dio/dio.new.cdc");
 		try {
 			serializer.write(cdcModel2, cdcFile2);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return;
 	}
 	
 	public static void convertCDCFile() {
 		CDCModelV1 cdcModel1 = null;
 		File cdcFile1 = new File("/home/derek/runtime-EclipseApplication/dio/dio.raw.cdc");
 		Serializer serializer = new Persister();
 		try {
 			cdcModel1 = serializer.read(CDCModelV1.class, cdcFile1);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		CDCModelV2 cdcModel2 = new CDCModelV2();
 		cdcModel2.getHead().setCreatetime(cdcModel1.getHead().getCreatetime());
 		cdcModel2.getHead().setOS(cdcModel1.getHead().getOS());
 		cdcModel2.getHead().setCreater(cdcModel1.getHead().getCreater());
 		cdcModel2.getHead().setFiletype(cdcModel1.getHead().getFiletype());
 		cdcModel2.setLastOpenedCodeFilename(cdcModel1.getLastOpenedCodeFilename());
 		cdcModel2.setLastOpenedSpecFilename(cdcModel1.getLastOpenedSpecFilename());
 		Vector<MapEntryV1> maps1 = cdcModel1.getMapEntries();
 		Vector<OpEntryV1> ops1 = cdcModel1.getOperationList();
 		Iterator it1 = ops1.iterator();
 		while(it1.hasNext()) {
 			OpEntryV1 entry1 = (OpEntryV1) it1.next();
 			String tempstr = entry1.getOperation();
 			System.out.println(tempstr);
 			Vector<String> operations = new Vector<String>();
 			while(tempstr.lastIndexOf('#') != -1) {
 				int index = tempstr.lastIndexOf('#');
 				if(tempstr.charAt(index-1) == '#') {
 					index--;
 				}
 				operations.add(0, tempstr.substring(index+1));
 				tempstr = tempstr.substring(0, index);
 			}
 			operations.add(0, tempstr);
 			if(operations.size() != 10) {
 				try {
 					throw new Exception("invalid operation record!");
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			String time = operations.get(0); // time
 			String os = operations.get(1); // os
 			String creater = operations.get(2); // author
 			if(operations.get(3).equals("add") && operations.get(4).equals("mapentry")) {
 				FolderMapTreeNodeV2 root = cdcModel2.getMapIdTree();
 				String parentfolderuuid = root.getData();
 				String codefilename2 = operations.get(5);
 				CodeSelectionV2 codeselpath2 = new CodeSelectionV2();
 				String codepath = operations.get(6);
 				int index = codepath.lastIndexOf('/');
 				codeselpath2.setSelCodeText(codepath.substring(0,index));
 				//codeselpath2.setSyntaxCodeText(codepath.substring(0,index)); // to be deleted
 				codepath = codepath.substring(index+1);
 				// index = codepath.indexOf('\\');
 				codeselpath2.setSelCodePath(codepath);
 				//codeselpath2.setSyntaxCodePath(codepath); // to be deleted
 				String specfilename2 = operations.get(7);
 				SpecSelectionV2 specselpath2 = new SpecSelectionV2();
 				String specpath = operations.get(8);
 				index = specpath.indexOf('/');
 				specselpath2.setPage(Integer.valueOf(specpath.substring(0,index)));
 				specpath = specpath.substring(index+1);
 				index = specpath.indexOf('/');
 				specselpath2.setLeft(Integer.valueOf(specpath.substring(0,index)));
 				specpath = specpath.substring(index+1);
 				index = specpath.indexOf('/');
 				specselpath2.setTop(Integer.valueOf(specpath.substring(0,index)));
 				specpath = specpath.substring(index+1);
 				index = specpath.indexOf('/');
 				specselpath2.setRight(Integer.valueOf(specpath.substring(0,index)));
 				specpath = specpath.substring(index+1);
 				index = specpath.indexOf('/');
 				specselpath2.setBottom(Integer.valueOf(specpath.substring(0,index)));
 				specpath = specpath.substring(index+1);
 				specselpath2.setPDFText(specpath);
 				String comment2 = operations.get(9);
 								
 				CEditor cEditor = null;
 				IWorkspace workspace = ResourcesPlugin.getWorkspace();
 				IWorkspaceRoot workspaceroot = workspace.getRoot();
 				IPath codefilepath = new Path(codefilename2);
 				codefilepath = codefilepath.removeFirstSegments(1); // remove "project:"
 				IFile codefile = (IFile) workspaceroot.getFile(codefilepath);
 				final FileEditorInput codeEditorInput = new FileEditorInput(codefile);
 				try 
 				{
 					IWorkbench workbench = PlatformUI.getWorkbench();
 					IWorkbenchWindow workbenchwindow = workbench.getActiveWorkbenchWindow();
 					IWorkbenchPage workbenchPage = workbenchwindow.getActivePage();
 					cEditor = (CEditor) workbenchPage.openEditor(codeEditorInput, "org.eclipse.cdt.ui.editor.CEditor");
 				} 
 				catch (PartInitException e) 
 				{
 					e.printStackTrace();
 				}
 				ITranslationUnit tu = (ITranslationUnit) cEditor.getInputCElement();
 				MyASTTree myASTTree =  new MyASTTree(tu);
 				index = codepath.indexOf('\\');
 				TextSelection rawsel = new TextSelection(Integer.valueOf(codepath.substring(0, index)), Integer.valueOf(codepath.substring(index+1)));
 				TextSelection synsel = myASTTree.adjustSelection1(rawsel);
 				IDocument doc = cEditor.getDocumentProvider().getDocument(cEditor.getEditorInput());
 				try {
 					codeselpath2.setSyntaxCodeText(doc.get(synsel.getOffset(), synsel.getLength()));
 					codeselpath2.setSyntaxCodePath(myASTTree.selection2Node1(synsel));
 				} catch (BadLocationException e) {
 					e.printStackTrace();
 				}
 				
 				cdcModel2.addMapEntry(time, os, creater, parentfolderuuid, codefilename2, codeselpath2, specfilename2, specselpath2, comment2);
 			} else if(operations.get(3).equals("del") && operations.get(4).equals("mapentry")) {
 				String codefilename2 = operations.get(5);
 				String codefileuuid2 = cdcModel2.getBody().codefiles.getFileEntryId(codefilename2);
 				CodeSelectionV2 codeselpath2 = new CodeSelectionV2();
 				String codepath = operations.get(6);
 				int index = codepath.indexOf('/');
 				codeselpath2.setSelCodeText(codepath.substring(0,index));
 				//codeselpath2.setSyntaxCodeText(codepath.substring(0,index)); // to be deleted
 				codepath = codepath.substring(index+1);
 				// index = codepath.indexOf('\\');
 				codeselpath2.setSelCodePath(codepath);
 				//codeselpath2.setSyntaxCodePath(codepath); // to be deleted
 				String specfilename2 = operations.get(7);
 				String specfileuuid2 = cdcModel2.getBody().specfiles.getFileEntryId(specfilename2);
 				SpecSelectionV2 specselpath2 = new SpecSelectionV2();
 				String specpath = operations.get(8);
 				index = specpath.indexOf('/');
 				specselpath2.setPage(Integer.valueOf(specpath.substring(0,index)));
 				specpath = specpath.substring(index+1);
 				index = specpath.indexOf('/');
 				specselpath2.setLeft(Integer.valueOf(specpath.substring(0,index)));
 				specpath = specpath.substring(index+1);
 				index = specpath.indexOf('/');
 				specselpath2.setTop(Integer.valueOf(specpath.substring(0,index)));
 				specpath = specpath.substring(index+1);
 				index = specpath.indexOf('/');
 				specselpath2.setRight(Integer.valueOf(specpath.substring(0,index)));
 				specpath = specpath.substring(index+1);
 				index = specpath.indexOf('/');
 				specselpath2.setBottom(Integer.valueOf(specpath.substring(0,index)));
 				specpath = specpath.substring(index+1);
 				specselpath2.setPDFText(specpath);
 				String comment2 = operations.get(9);
 				
 				CEditor cEditor = null;
 				IWorkspace workspace = ResourcesPlugin.getWorkspace();
 				IWorkspaceRoot workspaceroot = workspace.getRoot();
 				IPath codefilepath = new Path(codefilename2);
 				codefilepath = codefilepath.removeFirstSegments(1); // remove "project:"
 				IFile codefile = (IFile) workspaceroot.getFile(codefilepath);
 				final FileEditorInput codeEditorInput = new FileEditorInput(codefile);
 				try 
 				{
 					IWorkbench workbench = PlatformUI.getWorkbench();
 					IWorkbenchWindow workbenchwindow = workbench.getActiveWorkbenchWindow();
 					IWorkbenchPage workbenchPage = workbenchwindow.getActivePage();
 					cEditor = (CEditor) workbenchPage.openEditor(codeEditorInput, "org.eclipse.cdt.ui.editor.CEditor");
 				} 
 				catch (PartInitException e) 
 				{
 					e.printStackTrace();
 				}
 				ITranslationUnit tu = (ITranslationUnit) cEditor.getInputCElement();
 				MyASTTree myASTTree =  new MyASTTree(tu);
 				index = codepath.indexOf('\\');
 				TextSelection rawsel = new TextSelection(Integer.valueOf(codepath.substring(0, index)), Integer.valueOf(codepath.substring(index+1)));
 				TextSelection synsel = myASTTree.adjustSelection1(rawsel);
 				IDocument doc = cEditor.getDocumentProvider().getDocument(cEditor.getEditorInput());
 				try {
 					codeselpath2.setSyntaxCodeText(doc.get(synsel.getOffset(), synsel.getLength()));
 					codeselpath2.setSyntaxCodePath(myASTTree.selection2Node1(synsel));
 				} catch (BadLocationException e) {
 					e.printStackTrace();
 				}
 				
 				String uuid = cdcModel2.getBody().maps.getMapEntryId(codefileuuid2, codeselpath2, specfileuuid2, specselpath2, comment2);
 				cdcModel2.deleteMapEntry(time, os, creater, uuid);
 			} else {
 				try {
 					throw new Exception("unsupported operation record!");
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		File cdcFile2 = new File("/home/derek/runtime-EclipseApplication/dio/dio.new.cdc");
 		try {
 			serializer.write(cdcModel2, cdcFile2);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return;
 	}
 	
 	// test
 	public static void main(String[] args) {
 		// MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Info", "test end successfully!");
 		Display display = new Display();
 		Rectangle rect = display.getBounds();
 		Shell shell = new Shell(display);
 		shell.setText("coDoc test center");
 		shell.setSize(400, 300);
 		Point shellSize = shell.getSize();
 		shell.setLocation((rect.width-shellSize.x)/2, (rect.height-shellSize.y)/2);
 		
 		Button btnMode1 = new Button(shell, SWT.NONE);
 		btnMode1.setText("Mode 1");
 		btnMode1.setBounds(shellSize.x/4,20,shellSize.x/2,40);
 		btnMode1.setToolTipText("Mode 1:\n  Random operation test. For CDCDataCenter only.");
 		btnMode1.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				return;
 			}
 		});
 		
 		Button btnMode2 = new Button(shell, SWT.NONE);
 		btnMode2.setText("Mode 2");
 		btnMode2.setBounds(shellSize.x/4,60,shellSize.x/2,40);
 		btnMode2.setToolTipText("Mode 2:\n  Historical operation test. For CDCDataCenter only.");
 		btnMode2.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				CDCDataCenter cdcDataCenter = new CDCDataCenter();
 				
 				FileDialog dialog = new FileDialog(e.display.getActiveShell(),SWT.OPEN);
 				dialog.setFilterNames(new String[]{"coDoc files","All Files (*.*)"});
 				dialog.setFilterExtensions(new String[]{"*.cdc","*.*"});
 				// dialog.setFilterPath("");
 				dialog.setText("open");
 				String cdcFilename = dialog.open();
 				// MessageDialog.openInformation(e.display.getActiveShell(), "Info", cdcFilename);
 				
 				Serializer serializer = new Persister();
 				File cdcFile = new File(cdcFilename);
 				CDCModel cdcModel = null;
 				try {
 					cdcModel = serializer.read(CDCModel.class, cdcFile);
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 				
 				String tempFilename = cdcFilename + ".tmp";
 				File tempFile = new File(tempFilename);
 				CDCModel tempCDCModel = new CDCModel();
 				try {
 					serializer.write(tempCDCModel, tempFile);
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 				
 				Vector<OpEntry> ops = cdcModel.getHist().getOperationList();
 				Iterator it = ops.iterator();
 				while(it.hasNext()) {
 					OpEntry op = (OpEntry) it.next();
 					String tempstr = op.getOperation();
 					Vector<String> opstr = new Vector<String>();
 					while(tempstr.lastIndexOf('#') != -1) {
 						int index = tempstr.lastIndexOf('#');
 						opstr.add(0, tempstr.substring(index+1));
 						tempstr = tempstr.substring(0,index);
 					}
 					opstr.add(0,tempstr);
 					
 					if(opstr.get(3).equals("add") && opstr.get(4).equals("mapentry")) {
 						String uuid = opstr.get(5);
 						String parentuuid = opstr.get(6);
 						String folderPath = cdcModel.getFolderEntry(parentuuid).getFolderpath();
 						parentuuid = cdcDataCenter.getCDCModel(tempFilename).getBody().folders.getFolderEntryId(folderPath);
 						MapEntry entry = cdcModel.getMapEntry(uuid);
 						String codefilename = cdcModel.getCodeFilename(entry.getCodefileUUID());
 						String specfilename = cdcModel.getSpecFilename(entry.getSpecfileUUID());
 						cdcDataCenter.addLinkEntry(tempFilename, parentuuid, codefilename, entry.getCodeselpath(), specfilename, entry.getSpecselpath(), entry.getComment());
 					} else if(opstr.get(3).equals("edt") && opstr.get(4).equals("mapentry")) {
 						String olduuid = opstr.get(5);
 						String uuid = opstr.get(6);
 						MapEntry oldentry = cdcModel.getMapEntry(olduuid);
 						String oldcodefilename = cdcModel.getBody().codefiles.getFilename(oldentry.getCodefileUUID());
 						String oldcodefileuuid = cdcDataCenter.getCDCModel(tempFilename).getBody().codefiles.getFileEntryId(oldcodefilename);
 						CodeSelection codeselpath = oldentry.getCodeselpath();
 						String oldspecfilename = cdcModel.getBody().specfiles.getFilename(oldentry.getSpecfileUUID());
 						String oldspecfileuuid = cdcDataCenter.getCDCModel(tempFilename).getBody().specfiles.getFileEntryId(oldspecfilename);
 						SpecSelection specselpath = oldentry.getSpecselpath();
 						String comment = oldentry.getComment();
 						olduuid = cdcDataCenter.getCDCModel(tempFilename).getBody().maps.getMapEntryId(oldcodefileuuid, codeselpath, oldspecfileuuid, specselpath, comment);
 						MapEntry entry = cdcModel.getMapEntry(uuid);
 						String codefilename = cdcModel.getCodeFilename(entry.getCodefileUUID());
 						String specfilename = cdcModel.getSpecFilename(entry.getSpecfileUUID());
 						cdcDataCenter.editLinkEntry(tempFilename, olduuid, codefilename, entry.getCodeselpath(), specfilename, entry.getSpecselpath(), entry.getComment());
 					} else if(opstr.get(3).equals("add") && opstr.get(4).equals("codefileentry")) {
 					} else if(opstr.get(3).equals("add") && opstr.get(4).equals("specfileentry")) {
 					} else {
 						MessageDialog.openError(e.display.getActiveShell(), "Error", "Unsupported operation:\n"+op.getOperation());
 						return;
 					}
 				}
 				
 				// compare MapIdTree
 				FolderMapTreeNode[] tree = new FolderMapTreeNode[]{cdcModel.getMapIdTree(),cdcDataCenter.getCDCModel(tempFilename).getMapIdTree()};
 				String[] treecode = new String[tree.length];
 				for(int i=0; i<tree.length; i++) {
 					Queue<FolderMapTreeNode> queue = new LinkedList<FolderMapTreeNode>();
 					treecode[i] = "";
 					queue.add(tree[i]);
 					while(!queue.isEmpty()) {
 						FolderMapTreeNode node = queue.remove();
 						if(node.hasChildren()) {
 							FolderMapTreeNode[] children = node.getChildren();
 							treecode[i] += Integer.toString(children.length) + "#";
 							for(FolderMapTreeNode n : children) {
 								queue.add(n);
 							}							
 						} else {
 							treecode[i] += Integer.toString(0) + "#";
 						}
 					}
 				}
 				if(!treecode[0].equals(treecode[1])) {
 					MessageDialog.openError(e.display.getActiveShell(), "Error", "The structures of the two MapIdTree are different!");
 					return;
 				}
 				
 				// compare MapIdTree with LinkTree
 				MapSelectionFilter filter = new MapSelectionFilter();
 				filter.setSelector(MapSelectionFilter.ALLDATA);
 				filter.setSearchStr("");
 				FolderMapTreeNode mapTree = cdcDataCenter.getCDCModel(tempFilename).getMapIdTree();
 				EntryNode linkTree = cdcDataCenter.getLinkTree(tempFilename, filter).getChildren()[0];
 				Queue<FolderMapTreeNode> mapQueue = new LinkedList<FolderMapTreeNode>();
 				mapQueue.add(mapTree);
 				Queue<EntryNode> linkQueue = new LinkedList<EntryNode>();
 				linkQueue.add(linkTree);
 				while(!mapQueue.isEmpty() && !linkQueue.isEmpty()) {
 					FolderMapTreeNode mapnode = mapQueue.remove();
 					EntryNode linknode = linkQueue.remove();
 					if(!mapnode.getData().equals(((BaseEntry)linknode.getData()).uuid)) {
 						MessageDialog.openError(e.display.getActiveShell(), "Error", "The content of MapIdTree and LinkTree are different!");
 						return;
 					}
 					if(mapnode.getChildren().length != linknode.getChildren().length) {
 						MessageDialog.openError(e.display.getActiveShell(), "Error", "The structures of MapIdTree and LinkTree are different!");
 						return;
 					}
 					for(FolderMapTreeNode n : mapnode.getChildren()) {
 						mapQueue.add(n);
 					}
 					for(EntryNode n : linknode.getChildren()) {
 						linkQueue.add(n);
 					}
 				}
 				if(!mapQueue.isEmpty() || !linkQueue.isEmpty()) {
 					MessageDialog.openError(e.display.getActiveShell(), "Error", "The structures of MapIdTree and LinkTree are different!");
 					return;
 				}
 				
 				MessageDialog.openInformation(e.display.getActiveShell(), "Info", "Test end successfully!");
 				return;
 			}
 		});
 		
 		shell.open();
 		while(!shell.isDisposed()) {
 			if(!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 		display.dispose();
 		return;
 	}
 }
