 package cl.miempresa.accesos.principal;
 
import java.util.List;
 import java.util.logging.Logger;
 
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.MenuAdapter;
 import org.eclipse.swt.events.MenuEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.ui.part.ViewPart;
 
 import cl.altair.acceso.dao.EdificioDAO;
 import cl.altair.acceso.dao.EntityManagerHelper;
 import cl.altair.acceso.modelo.Dependencia;
 import cl.altair.acceso.modelo.Edificio;
 import cl.altair.accesos.principal.formularios.CreaDependencia;
 import cl.altair.utiles.generales.Piso;
 import cl.miempresa.accesos.modelo.providers.ArbolEdificioContentProvider;
 import cl.miempresa.accesos.modelo.providers.ArbolEdificioLabelProvider;
 import cl.miempresa.accesos.modelo.providers.TreeEdificio;
 
 public class ArbolEdificio extends ViewPart {
 	public ArbolEdificio() {
 	}
 	
 	private TreeViewer viewer;
 	public TreeViewer getViewer() {
 		return viewer;
 	}
 
 	public void setViewer(TreeViewer viewer) {
 		this.viewer = viewer;
 	}
 
 	private final static Logger traza = Logger.getLogger(ArbolEdificio.class.getName());
 	private Edificio elEdificio;
 
 	public void createPartControl(final Composite parent) {
 		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
 		final Tree tree = viewer.getTree();
 		GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
 		tree.setLayoutData(gd_tree);
 		gd_tree.widthHint = 309;
 		viewer.setContentProvider(new ArbolEdificioContentProvider());
 		viewer.setLabelProvider(new ArbolEdificioLabelProvider());
 		// Expand the tree
 		viewer.setAutoExpandLevel(2);
 		// Provide the input to the ContentProvider
 		viewer.setInput(getEdificio());
 		
 		// Create the popup menu
 		MenuManager menuMgr = new MenuManager();
 		final Menu menu = menuMgr.createContextMenu(viewer.getControl());
 		MenuItem menuItem = new MenuItem(menu, SWT.NONE);
 		menuItem.setText("Test");
 /*
 		menuMgr.addMenuListener(new IMenuListener() {
 			@Override
 			public void menuAboutToShow(IMenuManager manager) {
 				if(viewer.getSelection().isEmpty()) {
 					return;
 				}		      
 				if(viewer.getSelection() instanceof IStructuredSelection) {
 					// Get rid of existing menu items
 					MenuItem[] items = menu.getItems();
 					for (int i = 0; i < items.length; i++) {
 						((MenuItem) items[i]).dispose();
 					}
 					// Add menu items for current selection
 					MenuItem newItem = new MenuItem(menu, SWT.NONE);
 					newItem.setText("Menu for " + tree.getSelection()[0].getText());
 				}
 			}
 		}); */
 		menuMgr.setRemoveAllWhenShown(true);
 		viewer.getControl().setMenu(menu);
 		
 		menu.addMenuListener(new MenuAdapter() {
 			public void menuShown(MenuEvent e) {
 				// Get rid of existing menu items
 				MenuItem[] items = menu.getItems();
 				for (int i = 0; i < items.length; i++) {
 					((MenuItem) items[i]).dispose();
 				}
 				final Object itemTree = tree.getSelection()[0].getData();
 	        	if(itemTree instanceof Dependencia){	        	
 					// Add menu items for current selection
 					MenuItem newItem = new MenuItem(menu, SWT.NONE);
 					newItem.setText("Borrar Dependencia " + tree.getSelection()[0].getText());
 					newItem.addListener(SWT.Selection, new Listener() {
 					    public void handleEvent(Event event) {
 					 		
 							EdificioDAO edao = new EdificioDAO();
 							Edificio edificio = edao.findAll().get(0);
 							
 							for(Dependencia unaDependencia: edificio.getDependencias()){
 								if(unaDependencia.getIdentificador().equalsIgnoreCase(((Dependencia) itemTree).getIdentificador()) &&
 										unaDependencia.getPiso().intValue() == ((Dependencia)itemTree).getPiso().intValue()){
 									edificio.getDependencias().remove(unaDependencia);
 									break;
 								}
 							}
 							
 							//Actualiza el edificio
 							EntityManagerHelper.beginTransaction();
 					    	edao.update(edificio);
 					    	EntityManagerHelper.commit();
 					 		EntityManagerHelper.closeEntityManager();
 					 		
 					 		//Refresca el arbol con la nueva dependencia
 					 		TreeEdificio todoArbol = new TreeEdificio();
 							Edificio[] arbol = new Edificio[1];
 							arbol[0] = edificio;			
 							todoArbol.setEdificio(arbol);
 							todoArbol.setNombre(edificio.getNombre());
 					 		viewer.setInput(todoArbol);
 							viewer.refresh();
 
 					    }
 					});
 
 	        	} else if(itemTree instanceof Piso){
 					// Add menu items for current selection
 					MenuItem newItem = new MenuItem(menu, SWT.NONE);
 					newItem.setText("Agregar Dependencia");
 					newItem.addListener(SWT.Selection, new Listener() {
 					    public void handleEvent(Event event) {
 					        System.out.println("Menu item PISO selected");
 							CreaDependencia formNuevaDependencia = new CreaDependencia(parent.getShell(),0);
 							formNuevaDependencia.setPiso(((Piso) itemTree).getPiso());
 							formNuevaDependencia.open();
 							
 					 		//Refresca el arbol con la nueva dependencia
 							EdificioDAO edao = new EdificioDAO();
 							Edificio edificio = edao.findAll().get(0);
 					 		TreeEdificio todoArbol = new TreeEdificio();
 							Edificio[] arbol = new Edificio[1];
 							arbol[0] = edificio;			
 							todoArbol.setEdificio(arbol);
 							todoArbol.setNombre(edificio.getNombre());
 					 		viewer.setInput(todoArbol);
 							viewer.refresh();
 					    }
 					});
 	        	}
 			}
 		});
 /*
 	    tree.addListener(SWT.MouseDown, new Listener() {
 	        public void handleEvent(Event event) {
 	          Point point = new Point(event.x, event.y);
 	          TreeItem item = tree.getItem(point);
 	          if (item != null) {  
 	        	  Object itemTree = item.getData();
 	        	  if(itemTree instanceof Dependencia){
 	    	          if (event.button == 3) {
 	    	        	  System.out.println("Right click !");
 	    	          }
 	        		  System.out.println("Nombre: " + ((Dependencia)itemTree).getIdentificador());
 	        		  System.out.println("Tipo dependencia: " + ((Dependencia)itemTree).getTipo().getTipoDependencia());
 	        		  System.out.println("Mostrar pop up menu");	        		  
 	        	  }
 	          }
 	        }
 	      });
 */
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
 	
 	private TreeEdificio getEdificio(){
 		traza.info("Dibujando el arbol del edificio");
 		EdificioDAO edao = new EdificioDAO();
 		TreeEdificio todoArbol = new TreeEdificio();
		List<Edificio> edificios = edao.findAll();
		if(edificios.isEmpty())
			return null;
 		Edificio elEdificio = edao.findAll().get(0);
 
 		Edificio[] edificio = new Edificio[1];
 		edificio[0] = elEdificio;
 		todoArbol.setEdificio(edificio);
 		todoArbol.setNombre(elEdificio.getNombre());
 		return todoArbol;
 	}
 
 	public Edificio getElEdificio() {
 		return elEdificio;
 	}
 
 	public void setElEdificio(Edificio elEdificio) {
 		this.elEdificio = elEdificio;
 	} 
 }
