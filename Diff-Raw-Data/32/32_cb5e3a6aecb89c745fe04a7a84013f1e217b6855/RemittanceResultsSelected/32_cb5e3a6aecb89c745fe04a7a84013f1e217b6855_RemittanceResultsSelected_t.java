 /******* BEGIN LICENSE BLOCK *****
  * Versión: GPL 2.0/CDDL 1.0/EPL 1.0
  *
  * Los contenidos de este fichero están sujetos a la Licencia
  * Pública General de GNU versión 2.0 (la "Licencia"); no podrá
  * usar este fichero, excepto bajo las condiciones que otorga dicha 
  * Licencia y siempre de acuerdo con el contenido de la presente. 
  * Una copia completa de las condiciones de de dicha licencia,
  * traducida en castellano, deberá estar incluida con el presente
  * programa.
  * 
  * Adicionalmente, puede obtener una copia de la licencia en
  * http://www.gnu.org/licenses/gpl-2.0.html
  *
  * Este fichero es parte del programa opensiXen.
  *
  * OpensiXen es software libre: se puede usar, redistribuir, o
  * modificar; pero siempre bajo los términos de la Licencia 
  * Pública General de GNU, tal y como es publicada por la Free 
  * Software Foundation en su versión 2.0, o a su elección, en 
  * cualquier versión posterior.
  *
  * Este programa se distribuye con la esperanza de que sea útil,
  * pero SIN GARANTÍA ALGUNA; ni siquiera la garantía implícita 
  * MERCANTIL o de APTITUD PARA UN PROPÓSITO DETERMINADO. Consulte 
  * los detalles de la Licencia Pública General GNU para obtener una
  * información más detallada. 
  *
  * TODO EL CÓDIGO PUBLICADO JUNTO CON ESTE FICHERO FORMA PARTE DEL 
  * PROYECTO OPENSIXEN, PUDIENDO O NO ESTAR GOBERNADO POR ESTE MISMO
  * TIPO DE LICENCIA O UNA VARIANTE DE LA MISMA.
  *
  * El desarrollador/es inicial/es del código es
  *  FUNDESLE (Fundación para el desarrollo del Software Libre Empresarial).
  *  Nexis Servicios Informáticos S.L. - http://www.nexis.es
  *
  * Contribuyente(s):
  *  Alejandro González <alejandro@opensixen.org> 
  *
  * Alternativamente, y a elección del usuario, los contenidos de este
  * fichero podrán ser usados bajo los términos de la Licencia Común del
  * Desarrollo y la Distribución (CDDL) versión 1.0 o posterior; o bajo
  * los términos de la Licencia Pública Eclipse (EPL) versión 1.0. Una 
  * copia completa de las condiciones de dichas licencias, traducida en 
  * castellano, deberán de estar incluidas con el presente programa.
  * Adicionalmente, es posible obtener una copia original de dichas 
  * licencias en su versión original en
  *  http://www.opensource.org/licenses/cddl1.php  y en  
  *  http://www.opensource.org/licenses/eclipse-1.0.php
  *
  * Si el usuario desea el uso de SU versión modificada de este fichero 
  * sólo bajo los términos de una o más de las licencias, y no bajo los 
  * de las otra/s, puede indicar su decisión borrando las menciones a la/s
  * licencia/s sobrantes o no utilizadas por SU versión modificada.
  *
  * Si la presente licencia triple se mantiene íntegra, cualquier usuario 
  * puede utilizar este fichero bajo cualquiera de las tres licencias que 
  * lo gobiernan,  GPL 2.0/CDDL 1.0/EPL 1.0.
  *
  * ***** END LICENSE BLOCK ***** */
 
 package org.opensixen.bankoperations.form;
 
 
 import java.awt.BorderLayout;
 import java.awt.Cursor;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import org.compiere.apps.ConfirmPanel;
 import org.compiere.apps.Waiting;
 import org.compiere.grid.ed.VNumber;
 import org.compiere.minigrid.MiniTable;
 import org.compiere.process.DocAction;
 import org.compiere.swing.CButton;
 import org.compiere.swing.CLabel;
 import org.compiere.swing.CPanel;
 import org.compiere.util.CLogger;
 import org.compiere.util.Env;
 import org.compiere.util.Msg;
 import org.jdesktop.swingx.JXTaskPane;
 import org.opensixen.model.MRemittance;
 import org.opensixen.process.RemittanceCreate;
 import org.opensixen.process.RemittancePayments;
 
 /**
  * 
  * RemittanceResultsSelected
  *
  * @author Alejandro González
  * Nexis Servicios Informáticos http://www.nexis.es
  */
 
 public class RemittanceResultsSelected extends JPanel implements ActionListener {
 
 	private static final long serialVersionUID = 1L;
 	
 	/**
 	 * Descripcion de campos
 	 */
 
 	//Paneles y tablas
 	private static MiniTable remittanceselect  = new MiniTable();
 	private static CPanel remittanceselectpanel = new CPanel();
 //	private ConfirmPanel confirm =  new ConfirmPanel(true);
 	private CPanel buttonsPanel = new CPanel();
 	private CPanel minitablepanel = new CPanel();
 	private CPanel totalsPanel = new CPanel();
 	private JXTaskPane ParamsPanel = new JXTaskPane();
 	protected RemittanceFormPanel ParentPane =null;
 	private Waiting wait;
 	
 	int m_WindowNo=0;
 	protected CLogger log = CLogger.getCLogger(getClass());
 
 	private RemittanceParams params;
 	
 	private CLabel lAmtTotal;
 	protected static VNumber fAmtTotal;
 
 	private CLabel lNumReg;
 	protected static VNumber fNumReg;
 	
 	private CButton BGenerateFile = new CButton(Msg.translate(Env.getCtx(), "GenerateFile"));
 	private CButton BPostRemittance = new CButton(Msg.translate(Env.getCtx(), "Post Remittance"));
 	
 	public RemittanceResultsSelected(){
 		initComponents();
 	}
 	
 	/**
 	 * Constructor con panel padre
 	 */
 	
 	public RemittanceResultsSelected(RemittanceFormPanel panel){
 		ParentPane=panel;
 		initComponents();
 	}
 	
 	/**
 	 * Inicializacion componentes
 	 */
 	
 	private void initComponents(){
 		fillPicks();
 		minitablepanel.setLayout(new BorderLayout());
 		params= new RemittanceParams(); 
 		minitablepanel.add(new JScrollPane(remittanceselect),BorderLayout.CENTER);
 
 		ParamsPanel.setTitle(Msg.translate(Env.getCtx(), "Params"));
 		
 		BGenerateFile.addActionListener(this);
 		BPostRemittance.addActionListener(this);
 		
 		buttonsPanel.add(BGenerateFile);
 		buttonsPanel.add(BPostRemittance);
 		
 		totalsPanel.setLayout(new GridBagLayout());
 		totalsPanel.add( lAmtTotal,new GridBagConstraints( 0,0,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets( 2,2,2,2 ),0,0 ));
 		totalsPanel.add( fAmtTotal,new GridBagConstraints( 1,0,1,1,0.3,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets( 2,2,2,20 ),0,0 ));
 
 		totalsPanel.add( lNumReg,new GridBagConstraints( 2,0,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets( 2,2,2,2 ),0,0 ));
 		totalsPanel.add( fNumReg,new GridBagConstraints( 3,0,1,1,0.3,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets( 2,2,2,20 ),0,0 ));
 		
 		totalsPanel.add( buttonsPanel,new GridBagConstraints( 0,1,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets( 2,2,2,2 ),0,0 ));
 		ParamsPanel.add(params);
 		
 		this.setLayout(new BorderLayout());
 		this.add(ParamsPanel,BorderLayout.NORTH);
 		this.add(minitablepanel,BorderLayout.CENTER);
 		this.add(totalsPanel,BorderLayout.SOUTH);
 
 	}
 	
 	private void fillPicks(){
 		lAmtTotal= new CLabel();
 		fAmtTotal = new VNumber();
 		fAmtTotal.setReadWrite(false);
 		lAmtTotal.setText(Msg.translate(Env.getCtx(), "GrandTotal"));
 		lAmtTotal.setLabelFor(fAmtTotal);
 		
 		lNumReg= new CLabel();
 		fNumReg = new VNumber();
 		fNumReg.setReadWrite(false);
 		lNumReg.setText(Msg.translate(Env.getCtx(), "NumReg"));
 		lNumReg.setLabelFor(fNumReg);
 
 	}
 
 	
 	public static CPanel getPanel(){
 		return remittanceselectpanel;
 	}
 	
 	public static MiniTable getTable(){
 		return remittanceselect;
 	}
 
 	
 	/**
 	 * Procede a crear la remesa en base de dato
 	 * @return
 	 */
 
 	public MRemittance createRemittanceFile(){
 		return createRemittanceFile(0);
 		
 	}
 	
 	public MRemittance createRemittanceFile(int remittance_id){
 		RemittanceCreate remittance = new RemittanceCreate();
 		remittance.setPanelValues(params);
 		return remittance.Create(remittance_id);
 		
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 
 		if(arg0.getSource().equals(BGenerateFile)){
 			//Comprobar si ya existe la remesa y estamos editando o la creamos nueva
 			//Si existe una remesa seleccionada en el panel de remesas es que estamos editando, en otro caso creando nueva
 			if(ParentPane!=null){
 				MRemittance remit;
 				RemittanceSearch search = ParentPane.getPanelSearch();
 				if(search.getSelectedRow()==-1)
 					remit=createRemittanceFile();
 				else
 					remit=createRemittanceFile(search.getSelectedRow());
 			}
 		}else if(arg0.getSource().equals(BPostRemittance)){
 			if(ParentPane!=null){
 				RemittanceSearch search = ParentPane.getPanelSearch();
 				MRemittance remit;
 				//Creamos Remesa
 				setBusy(true);
 				//Comprobamos que la remesa sea posible completar, es decir que tenga el estado borrador
 				//y exista una remesa seleccionada
 				remit = new MRemittance(Env.getCtx(),search.getSelectedRow(),null);
 				//Completamos la remesa
 				if(remit!=null && (remit.getDocStatus().equals(DocAction.STATUS_Drafted) || remit.getDocStatus().equals(DocAction.STATUS_InProgress) )){
 					RemittancePayments remitpayments= new RemittancePayments();			
 					remitpayments.doIt(remit,true);
 				}
 			
 				//Una vez acabados los procesos pasamos la remesa creada al listado actualizado
 			
 				search.getRemittanceLookup().refresh();
 			}
 			setBusy(false);
 		}
 		
 	}
 	
 	/**
 	 * Espera de proceso remesa
 	 * @param busy
 	 */
 	
 	private void setBusy(boolean busy){
 		if(busy){
 			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 			wait = new Waiting(new JFrame(), Msg.translate(Env.getCtx(), "Processing"), true, 0);
 
 		}
 		else{
 			setCursor(Cursor.getDefaultCursor());
 			wait.dispose();
 			JOptionPane.showMessageDialog(null, Msg.translate(Env.getCtx(), "C_Remittance"), Msg.translate(Env.getCtx(), "RemittanceOK"), JOptionPane.INFORMATION_MESSAGE);
 
 		}
 	}
 	
 	
 }
