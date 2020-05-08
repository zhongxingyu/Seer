 package webApplication.grafica;
 
 import java.awt.Component;
 import java.io.File;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.ImageIcon;
 import javax.swing.JTree;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.TreePath;
 
 import webApplication.business.Componente;
 import webApplication.business.ComponenteAlternative;
 import webApplication.business.ComponenteComposto;
 import webApplication.business.ComponenteMolteplice;
 import webApplication.business.Immagine;
 import webApplication.business.Link;
 import webApplication.business.Testo;
 import webApplication.grafica.MainWindow.StatusBarGreen;
 
 /**
  * Il renderer dei nodi limitatamente per le icone a seconda del tipo di
  * componente che rappresenta
  * 
  * @author Andrea
  * 
  */
 public class CustomCellRenderer extends DefaultTreeCellRenderer {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8057343107481280548L;
 	private ImageIcon homeIcon;
 	private ImageIcon textIcon;
 	private ImageIcon imageIcon;
 	private ImageIcon linkIcon;
 	private ImageIcon compositeIcon;
 	private ImageIcon alternativeIcon;
 	//private ImageIcon errorIcon;
 	//private ImageIcon homeErrIcon;
 	private ImageIcon textErrIcon;
 	private ImageIcon imageErrIcon;
 	private ImageIcon linkErrIcon;
 	private ImageIcon compositeErrIcon;
 	private ImageIcon alternativeErrIcon;
 //	private ImageIcon warningIcon;
 
 	private static String BASEPATH = "icon/";
 	private static String HOMEICON = "Home.png";
 	private static String TEXTICONNAME = "Testo.png";
 	private static String IMAGEICONNAME = "Immagine.png";
 	private static String LINKICONNAME = "Link.png";
 	private static String ALTERNATIVEICONNAME = "Alternativa.png";
 	private static String COMPOSITEICONNAME = "Composto.png";
 	private static String TEXTERRICONNAME = "TestoErr.png";
 	private static String IMAGEERRICONNAME = "ImmagineErr.png";
 	private static String LINKERRICONNAME = "LinkErr.png";
 	private static String ALTERNATIVEERRICONNAME = "AlternativaErr.png";
 	private static String COMPOSITEERRICONNAME = "CompostoErr.png";
 	//private static String ERRORICONNAME = "error.png";
 //	private static String WARNINGICONNAME = "warning.png";
 
 	private static String ERRORTOOLTIPTEXT = "The choosen component name is already taken";
 //	private static String WARNINGTOOLTIPTEXT = "The element contains al least one element. Consider adding more internal components";
 	
 	private static final String URL_REGEX = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
 	private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);
 
 	/**
 	 * Il costruttore del renderer per l'albero
 	 */
 	public CustomCellRenderer() {
 		homeIcon = createImageIcon(BASEPATH + HOMEICON);
 		textIcon = createImageIcon(BASEPATH + TEXTICONNAME);
 		imageIcon = createImageIcon(BASEPATH + IMAGEICONNAME);
 		linkIcon = createImageIcon(BASEPATH + LINKICONNAME);
 		compositeIcon = createImageIcon(BASEPATH + COMPOSITEICONNAME);
 		alternativeIcon = createImageIcon(BASEPATH + ALTERNATIVEICONNAME);
 		//errorIcon = createImageIcon(BASEPATH + ERRORICONNAME);
 		textErrIcon = createImageIcon(BASEPATH + TEXTERRICONNAME);
 		imageErrIcon = createImageIcon(BASEPATH + IMAGEERRICONNAME);
 		linkErrIcon = createImageIcon(BASEPATH + LINKERRICONNAME);
 		compositeErrIcon = createImageIcon(BASEPATH + COMPOSITEERRICONNAME);
 		alternativeErrIcon = createImageIcon(BASEPATH + ALTERNATIVEERRICONNAME);
 		//errorErrIcon = createImageIcon(BASEPATH + ERRORICONNAME);
 //		warningIcon = createImageIcon(BASEPATH + WARNINGICONNAME);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
 		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
 		DisabledNode node = (DisabledNode) value;
 		if (node != tree.getModel().getRoot()) {
 			
 //			if (node.isCorrect == false) {
 //				setIcon(errorIcon);
 //				setToolTipText(ERRORTOOLTIPTEXT);
 //				return this;
 //			}
 			
 			if (hasError(tree, node)) {
 				node.isCorrect = false;
				boolean b = MainWindow.albero.isCorrect();
				MainWindow.btnGenXML.setEnabled(b);
				MainWindow.setStatusBar(b);
 
 				return this;
 			}
 			Componente comp = (Componente) node.getUserObject();
 			// Java SE6 non supporta lo switch con String solo da Java SE 7
 			String objType = comp.getType();
 			if (objType.equals(Testo.TEXTTYPE)) {
 				setIcon(textIcon);
 			} else if (objType.equals(Immagine.IMAGETYPE)) {
 				setIcon(imageIcon);
 			} else if (objType.equals(Link.LINKTYPE)) {
 				setIcon(linkIcon);
 			} else if (objType.equals(ComponenteComposto.COMPOSTOTYPE)) {
 				setIcon(compositeIcon);
 			} else if (objType.equals(ComponenteAlternative.ALTERNATIVETYPE)) {
 				setIcon(alternativeIcon);
 			}
 			setToolTipText(null);// fa in modo che il tooltip sparica spostandosi da un nodo con errore/warning
 			if (!node.getEnabled()) {
 				System.out.println("Nodo disabilitato");
 				setEnabled(false);
 			}
 		} else {
 			setIcon(homeIcon);
 			if(node.getChildCount()==0) {
 				node.isCorrect = false;
				boolean b = MainWindow.albero.isCorrect();
				MainWindow.btnGenXML.setEnabled(b);
				MainWindow.setStatusBar(b);
 						
 				return this;
 			}
 			setToolTipText(null);// fa in modo che il tooltip sparica spostandosi da un nodo con errore/warning
 			
 		}
 		node.isCorrect = true;
		boolean b = MainWindow.albero.isCorrect();
		MainWindow.btnGenXML.setEnabled(b);
		MainWindow.setStatusBar(b);
 
 		return this;
 	}
 
 	protected boolean hasError(JTree tree, DisabledNode node) {
 		try {
 			Componente comp = (Componente) node.getUserObject();
 			String objType = comp.getType();
 			TreePanel panel = (TreePanel) (((tree.getParent()).getParent()).getParent());
 			// Recupero il path degli elementi con quel nome
 			Vector<TreePath> elPath = panel.getPathForName((((Componente) node.getUserObject()).getNome()));
 			if (elPath.size() != 1) {
 				// Un solo elemento con quel nome 
 				if (elPath.contains(new TreePath(node.getPath()))) {
 					// Se il path di node contiene tra quelli con nome non
 					// consentito setto l'icona di errore e setto il tooltip
 					
 					// Java SE6 non supporta lo switch con String solo da Java SE 7
 					
 					if (objType.equals(Testo.TEXTTYPE)) {
 						setIcon(textErrIcon);
 					} else if (objType.equals(Immagine.IMAGETYPE)) {
 						setIcon(imageErrIcon);
 					} else if (objType.equals(Link.LINKTYPE)) {
 						setIcon(linkErrIcon);
 					} else if (objType.equals(ComponenteComposto.COMPOSTOTYPE)) {
 						setIcon(compositeErrIcon);
 					} else if (objType.equals(ComponenteAlternative.ALTERNATIVETYPE)) {
 						setIcon(alternativeErrIcon);
 					}
 					//setIcon(errorIcon);
 					setToolTipText(ERRORTOOLTIPTEXT);
 					return true;
 				}
 			} else if (((Componente)node.getUserObject()).getNome().isEmpty()) {
 				if (objType.equals(Testo.TEXTTYPE)) {
 					setIcon(textErrIcon);
 				} else if (objType.equals(Immagine.IMAGETYPE)) {
 					setIcon(imageErrIcon);
 				} else if (objType.equals(Link.LINKTYPE)) {
 					setIcon(linkErrIcon);
 				} else if (objType.equals(ComponenteComposto.COMPOSTOTYPE)) {
 					setIcon(compositeErrIcon);
 				} else if (objType.equals(ComponenteAlternative.ALTERNATIVETYPE)) {
 					setIcon(alternativeErrIcon);
 				}
 
 				setToolTipText(ERRORTOOLTIPTEXT);
 				return true;
 			} if (((Componente)node.getUserObject()).getCategoria().isEmpty()) {
 				if (objType.equals(Testo.TEXTTYPE)) {
 					setIcon(textErrIcon);
 				} else if (objType.equals(Immagine.IMAGETYPE)) {
 					setIcon(imageErrIcon);
 				} else if (objType.equals(Link.LINKTYPE)) {
 					setIcon(linkErrIcon);
 				} else if (objType.equals(ComponenteComposto.COMPOSTOTYPE)) {
 					setIcon(compositeErrIcon);
 				} else if (objType.equals(ComponenteAlternative.ALTERNATIVETYPE)) {
 					setIcon(alternativeErrIcon);
 				}
 
 				setToolTipText(ERRORTOOLTIPTEXT);
 				return true;
 			} else if ((!((Componente) node.getUserObject()).isSimple()) && (((ComponenteMolteplice) node.getUserObject()).getOpzioni().size() < 1)) {
 				if (objType.equals(Testo.TEXTTYPE)) {
 					setIcon(textErrIcon);
 				} else if (objType.equals(Immagine.IMAGETYPE)) {
 					setIcon(imageErrIcon);
 				} else if (objType.equals(Link.LINKTYPE)) {
 					setIcon(linkErrIcon);
 				} else if (objType.equals(ComponenteComposto.COMPOSTOTYPE)) {
 					setIcon(compositeErrIcon);
 				} else if (objType.equals(ComponenteAlternative.ALTERNATIVETYPE)) {
 					setIcon(alternativeErrIcon);
 				}
 
 				setToolTipText(ERRORTOOLTIPTEXT);
 				return true;
 			} else if (((Componente)node.getUserObject()).getType().equals(Immagine.IMAGETYPE)) {
 				File file = new File(((Immagine)node.getUserObject()).getPath());
 				if (!file.exists()) {
 					if (objType.equals(Testo.TEXTTYPE)) {
 						setIcon(textErrIcon);
 					} else if (objType.equals(Immagine.IMAGETYPE)) {
 						setIcon(imageErrIcon);
 					} else if (objType.equals(Link.LINKTYPE)) {
 						setIcon(linkErrIcon);
 					} else if (objType.equals(ComponenteComposto.COMPOSTOTYPE)) {
 						setIcon(compositeErrIcon);
 					} else if (objType.equals(ComponenteAlternative.ALTERNATIVETYPE)) {
 						setIcon(alternativeErrIcon);
 					}
 
 					setToolTipText(ERRORTOOLTIPTEXT);
 					return true;
 				}
 			} else if (((Componente)node.getUserObject()).getType().equals(Testo.TEXTTYPE)) {
 				if (((Testo)node.getUserObject()).getTesto().isEmpty()) {
 					if (objType.equals(Testo.TEXTTYPE)) {
 						setIcon(textErrIcon);
 					} else if (objType.equals(Immagine.IMAGETYPE)) {
 						setIcon(imageErrIcon);
 					} else if (objType.equals(Link.LINKTYPE)) {
 						setIcon(linkErrIcon);
 					} else if (objType.equals(ComponenteComposto.COMPOSTOTYPE)) {
 						setIcon(compositeErrIcon);
 					} else if (objType.equals(ComponenteAlternative.ALTERNATIVETYPE)) {
 						setIcon(alternativeErrIcon);
 					}
 
 					setToolTipText(ERRORTOOLTIPTEXT);
 					return true;
 				}
 			} else if (((Componente)node.getUserObject()).getType().equals(Link.LINKTYPE)) {
 				if ((((Link)node.getUserObject()).getTesto().isEmpty()) || (!isPathCorrect(((Link)node.getUserObject()).getUri()))) {
 					if (objType.equals(Testo.TEXTTYPE)) {
 						setIcon(textErrIcon);
 					} else if (objType.equals(Immagine.IMAGETYPE)) {
 						setIcon(imageErrIcon);
 					} else if (objType.equals(Link.LINKTYPE)) {
 						setIcon(linkErrIcon);
 					} else if (objType.equals(ComponenteComposto.COMPOSTOTYPE)) {
 						setIcon(compositeErrIcon);
 					} else if (objType.equals(ComponenteAlternative.ALTERNATIVETYPE)) {
 						setIcon(alternativeErrIcon);
 					}
 
 					setToolTipText(ERRORTOOLTIPTEXT);
 					return true;
 				}
 			}
 			//TODO controllare link url
 		} catch (NullPointerException e) {
 		} catch (ClassCastException e) {
 		}
 		return false;
 	}
 
 
 	/**
 	 * Carica l'immagine partendo dal suo path
 	 * 
 	 * @param path
 	 *            Il path dell'immagine
 	 * @return L'immagine caricata
 	 */
 	private static ImageIcon createImageIcon(String path) {
 		return new ImageIcon(path);
 	}
 	
 	private static boolean isPathCorrect(String path) {
 		Matcher urlMatcher = URL_PATTERN.matcher(path);
 		return urlMatcher.matches();
 	}
 
 }
