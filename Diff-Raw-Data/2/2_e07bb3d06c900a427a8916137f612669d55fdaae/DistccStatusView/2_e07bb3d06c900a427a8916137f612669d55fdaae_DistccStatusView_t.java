 package com.ghvandoorn.distcc.views;
 
 
 import java.io.BufferedInputStream;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.ui.IPartListener;
 import org.eclipse.ui.IPartService;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 
 
 
 public class DistccStatusView extends ViewPart implements IPartListener {
 
 	/**
 	 * The ID of the view as specified by the extension.
 	 */
 	public static final String ID = "com.ghvandoorn.distcc.views.DistccStatusView";
 	public static final String DISTCC_STATE_LOCATION = System.getProperty("user.home") + "/.distcc/state/";
 
 	private TableViewer viewer;
 	private Thread mUpdateThread = null;
 	
 	// See http://distcc.sourcearchive.com/documentation/3.1-3.1build1/src_2state_8h-source.html
 	private enum DccPhase {
 		STARTUP, BLOCKED, CONNECT, CPP, SEND, COMPILE, RECEIVE, DONE
 	}
 
 	static class DccState {
 		
 		private String mStateFilename = null;
 		private long mSize = -1;
 		private long mMagic = -1;
 		private long mPID = -1;
 		private String mFilename = null;
 		private String mHost = null;
 		private int mSlot = -1;
 		private DccPhase mPhase = DccPhase.COMPILE;
 		private long mModificationTime = 0;
 		private static final ByteOrder mByteOrder = ByteOrder.nativeOrder();
 
 		public static DccState create(File file) {
 			DccState result = new DccState();
 			result.mStateFilename = file.getAbsolutePath();
 			result.mModificationTime = file.lastModified();
 			if (result.mModificationTime == 0L) {
 				return null;
 			}
 			if ( (System.currentTimeMillis() - result.mModificationTime) > 60000) {
 				file.delete();
 				return null;
 			}
 			DataInputStream in = null;
 			try {
 				in = new DataInputStream(
 						new BufferedInputStream(
 								new FileInputStream(result.mStateFilename)));
 
 
 				result.mSize = reverse(in.readLong());
 				if (result.mSize != 296) { // Version change?
 					return null;
 				}
 				result.mMagic = reverse(in.readLong());
 				if (result.mMagic != 0x44494800) {  // Version change?
 					return null;
 				}
 				result.mPID = reverse(in.readLong());
 				byte[] str = new byte[128];
 				in.read(str);
 				result.mFilename = new String(str);
 				result.mFilename = result.mFilename.trim();
 				if (result.mFilename.isEmpty()) {
 					return null;
 				}
 				in.read(str);
 				result.mHost = new String(str);
				result.mHost = result.mHost.trim();
 				if (result.mHost.isEmpty()) {
 					return null;
 				}
 				result.mSlot = reverse(in.readInt());
 				int phase_nr = reverse(in.readInt());
 				if (phase_nr >= 0 && phase_nr < DccPhase.values().length) {
 					result.mPhase = DccPhase.values()[phase_nr];
 				}
 				return result;
 
 			} catch (IOException e) {
 			} finally {
 				if (in != null) {
 					try {
 						in.close();
 					} catch (IOException e) {
 					}
 				}
 			}
 			return null;
 		}
 		public DccState() {
 		}
 		static long reverse(long val) {
 			ByteBuffer bbuf = ByteBuffer.allocate(8);
 			return bbuf.order(ByteOrder.BIG_ENDIAN).putLong(val).order(mByteOrder).getLong(0);
 		}
 		static int reverse(int val) {
 			ByteBuffer bbuf = ByteBuffer.allocate(4);
 			return bbuf.order(ByteOrder.BIG_ENDIAN).putInt(val).order(mByteOrder).getInt(0);
 		}
 		public String getFilename() {
 			return mFilename;
 		}
 		public long getPID() {
 			return mPID;
 		}
 		public long getMagic() {
 			return mMagic;
 		}
 		public String getHost() {
 			return mHost;
 		}
 		public int getSlot() {
 			return mSlot;
 		}
 		public DccPhase getPhase() {
 			return mPhase;
 		}
 		public String getStateFilename() {
 			return mStateFilename;
 		}
 		public boolean isRunning() {
 			File process = new File("/proc/" + mPID);
 			return process.exists();
 		}
 	}
 	 
 	class ViewContentProvider implements IStructuredContentProvider {
 		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
 		}
 		public void dispose() {
 		}
 		public Object[] getElements(Object parent) {
 			return (Object[]) parent;
 		}
 	}
 	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
 		public String getColumnText(Object obj, int index) {
 			return getText(obj);
 		}
 		public Image getColumnImage(Object obj, int index) {
 			return getImage(obj);
 		}
 		public Image getImage(Object obj) {
 			return PlatformUI.getWorkbench().
 					getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
 		}
 	}
 	class StateSorter extends ViewerComparator {
 		@Override
 	    public int compare(Viewer viewer, Object e1, Object e2) {
 	        if (e1 instanceof DccState && e2 instanceof DccState) {
 	        	DccState state1 = (DccState) e1;
 	        	DccState state2 = (DccState) e2;
 	        	if (state1.getHost().compareToIgnoreCase(state2.getHost()) == 0) {
 	        		if (state1.getSlot() == state2.getSlot()) {
 	        			return 0;
 	        		} else if (state1.getSlot() > state2.getSlot()) {
 	        			return 1;
 	        		} else {
 	        			return -1;
 	        		}
 	        	} else {
 	        		return state1.getHost().compareToIgnoreCase(state2.getHost());
 	        	}
 	            
 	        }
 	        throw new IllegalArgumentException("Not comparable: " + e1 + " " + e2);
 	    }
 	}
 
 	class UpdateThread implements Runnable {
 
 		private void update() {
 			File dir = new File(DISTCC_STATE_LOCATION);
 			File[] files = dir.listFiles();
 			if (files == null) {
 				return;
 			}
 			final List<DccState> list = new ArrayList<DccState>();
 			for (File file : files) {
 				DccState state = DccState.create(file);
 				if (state != null && state.isRunning()) {
 					list.add(state);
 				}
 			}
 			Display.getDefault().asyncExec(new Runnable() {
 				public void run() {
 					if (!viewer.getControl().isDisposed()) {
 						viewer.setInput(list.toArray());
 					}
 				}
 			});
 		}
 
 		@Override
 		public void run() {
 			while (!Thread.currentThread().isInterrupted()) {
 				update();
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					return;
 				}
 			}
 		}
 	}
 
 	/**
 	 * The constructor.
 	 */
 	public DistccStatusView() {
 	}
 
 	private void start() {
 		File distccDir = new File(DISTCC_STATE_LOCATION);
 		if (distccDir.exists()) {
 			mUpdateThread = new Thread(new UpdateThread());
 			mUpdateThread.start();
 		} else {
 			System.err.println("Distcc state directory not found: " + DISTCC_STATE_LOCATION);
 		}
 	}
 
 	private void stop() {
 		if (mUpdateThread != null) {
 			mUpdateThread.interrupt();
 		}
 	}
 
 	/**
 	 * This is a callback that will allow us
 	 * to create the viewer and initialize it.
 	 */
 	public void createPartControl(Composite parent) {
 		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
 		viewer.setContentProvider(new ViewContentProvider());
 		viewer.setLabelProvider(new ViewLabelProvider());
 		viewer.setComparator(new StateSorter());
 		
 		Table table = viewer.getTable();
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 		
 		setupColumns();
 
 		// Create the help context id for the viewer's control
 		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "com.ghvandoorn.distcc.viewer");
 
 		start();
 	}
 	
 	@Override
 	public void init(IViewSite site) throws PartInitException {
 		super.init(site);
 		IPartService service = (IPartService) getSite().getService(IPartService.class);
 		service.addPartListener(this);
 	}
 	
 	public void setupColumns() {
 		TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
 		col.getColumn().setWidth(200);
 		col.getColumn().setText("Host");
 		col.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				DccState state = (DccState) element;
 				return state.getHost();
 			}
 		});
 		col = new TableViewerColumn(viewer, SWT.NONE);
 		col.getColumn().setWidth(100);
 		col.getColumn().setText("Slot");
 		col.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				DccState state = (DccState) element;
 				return String.valueOf(state.getSlot());
 			}
 		});
 		col = new TableViewerColumn(viewer, SWT.NONE);
 		col.getColumn().setWidth(250);
 		col.getColumn().setText("File");
 		col.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				DccState state = (DccState) element;
 				return state.getFilename();
 			}
 		});
 		col = new TableViewerColumn(viewer, SWT.NONE);
 		col.getColumn().setWidth(150);
 		col.getColumn().setText("Phase");
 		col.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				DccState state = (DccState) element;
 				return state.getPhase().toString();
 			}
 		});
 		col = new TableViewerColumn(viewer, SWT.NONE);
 		col.getColumn().setWidth(100);
 		col.getColumn().setText("PID");
 		col.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				DccState state = (DccState) element;
 				return String.valueOf(state.getPID());
 			}
 		});
 	}
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 
 	@Override
 	public void partActivated(IWorkbenchPart part) {
 	}
 
 	@Override
 	public void partBroughtToTop(IWorkbenchPart part) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void partClosed(IWorkbenchPart part) {
 		stop();
 	}
 
 	@Override
 	public void partDeactivated(IWorkbenchPart part) {
 	}
 
 	@Override
 	public void partOpened(IWorkbenchPart part) {
 		// TODO Auto-generated method stub
 	}
 }
