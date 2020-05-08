 package cl.miempresa.accesos.principal;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.ui.part.ViewPart;
 
 import cl.altair.acceso.dao.DirectorioDAO;
 import cl.altair.acceso.dao.EdificioDAO;
 import cl.altair.acceso.dao.EntityManagerHelper;
 import cl.altair.acceso.modelo.Dependencia;
 import cl.altair.acceso.modelo.Directorio;
 import cl.altair.acceso.modelo.Edificio;
 import cl.miempresa.accesos.modelo.providers.ArbolDirectorioContentProvider;
 import cl.miempresa.accesos.modelo.providers.ArbolDirectorioLabelProvider;
 import cl.miempresa.accesos.modelo.providers.TreeDirectorio;
 
 public class ArbolDirectorio extends ViewPart {
 	
 	private TreeViewer viewer;
 	private final static Logger traza = Logger.getLogger(ArbolDirectorio.class.getName());
 	private Edificio elEdificio;
 
 	public void createPartControl(Composite parent) {
 		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
 		final Tree tree = viewer.getTree();
 		GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
 		tree.setLayoutData(gd_tree);
 		gd_tree.widthHint = 309;
 		viewer.setContentProvider(new ArbolDirectorioContentProvider());
 		viewer.setLabelProvider(new ArbolDirectorioLabelProvider());
 		// Expand the tree
 		viewer.setAutoExpandLevel(2);
 		// Provide the input to the ContentProvider
 		viewer.setInput(getDirectorio());
 
 	    tree.addListener(SWT.MouseDown, new Listener() {
 	        public void handleEvent(Event event) {
 	          Point point = new Point(event.x, event.y);
 	          if (event.button == 3) {
 	        	  System.out.println("Right click !");
 	          }
 	          TreeItem item = tree.getItem(point);
 	          if (item != null) {  
 	        	  Object itemTree = item.getData();
 	        	  if(itemTree instanceof Directorio){
 	        		  System.out.println("Nombre: " + ((Directorio)itemTree).getNombre());
 	        		  System.out.println("ID: " + ((Directorio)itemTree).getId());
 	        		  System.out.println("Miembros: " + ((Directorio)itemTree).getMiembros());
 	        		  System.out.println("Mostrar pop up menu");
 	        	  }
 	            System.out.println("Mouse down: " + item);
 	          }
 	        }
 	      });
 
 		viewer.addDoubleClickListener(new IDoubleClickListener() {
 			@Override
 			public void doubleClick(DoubleClickEvent event) {
 				System.out.println("doble click");
 				TreeViewer viewer = (TreeViewer) event.getViewer();
 				IStructuredSelection thisSelection = (IStructuredSelection) event.getSelection();
 				Object selectedNode = thisSelection.getFirstElement();
 				viewer.setExpandedState(selectedNode,!viewer.getExpandedState(selectedNode));
 			}
 		});
 
 		
 		viewer.getTree().addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyReleased(final KeyEvent e) {
 				if (e.keyCode == SWT.DEL) {
 					final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
 					if (selection.getFirstElement() instanceof Dependencia) {
 						Dependencia o = (Dependencia) selection.getFirstElement();
 						traza.finest("Borra la dependencia a traves del teclado");
 						// TODO Delete the selected element from the model
 					}
 
 				}
 			}
 		});
 	}
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 	
 	private TreeDirectorio getDirectorio(){
 		traza.info("Dibujando el arbol del directorio");
 		DirectorioDAO ddao = new DirectorioDAO();
 		TreeDirectorio todoArbol = new TreeDirectorio();
 		Directorio unDirectorio;
 		
 		List<Directorio> listaDirectorio = ddao.findAll();
 		traza.info("Rescata el primer directorio que encuentra");
		if(listaDirectorio.isEmpty())
			return null;
 		unDirectorio = listaDirectorio.get(0);
 		//Entrega al objeto arbol un arreglo de 1 elemento con el directorio encontrado
 		Directorio[] eldirectorio = new Directorio[1];
 		eldirectorio[0] = unDirectorio;
 		todoArbol.setDirectorio(eldirectorio);
 		return todoArbol;
 	}
 
 	public Edificio getElEdificio() {
 		return elEdificio;
 	}
 
 	public void setElEdificio(Edificio elEdificio) {
 		this.elEdificio = elEdificio;
 	}
 	public TreeViewer getViewer() {
 		return viewer;
 	}
 
 	public void setViewer(TreeViewer viewer) {
 		this.viewer = viewer;
 	}
 
 }
