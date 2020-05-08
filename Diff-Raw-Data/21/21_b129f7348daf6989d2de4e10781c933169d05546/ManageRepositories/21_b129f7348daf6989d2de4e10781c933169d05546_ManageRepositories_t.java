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
  *  Indeos Consultoria S.L. - http://www.indeos.es
  *
  * Contribuyente(s):
  *  Eloy Gómez García <eloy@opensixen.org> 
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
 
 package org.opensixen.swing;
 
 import java.awt.BorderLayout;
 import java.awt.Dialog;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.HeadlessException;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseEvent;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Properties;
 
 import javax.swing.BoxLayout;
 
 import org.compiere.apps.ADialog;
 import org.compiere.swing.CButton;
 import org.compiere.swing.CDialog;
 import org.compiere.swing.CLabel;
 import org.compiere.swing.CPanel;
 import org.compiere.swing.CScrollPane;
 import org.compiere.swing.CTextField;
 import org.compiere.util.Env;
 import org.compiere.util.Msg;
 import org.jdesktop.swingx.JXTaskPane;
 import org.opensixen.interfaces.IBeanProvider;
 import org.opensixen.model.ColumnDefinition;
 import org.opensixen.p2.P2;
 import org.opensixen.p2.RepositoryModel;
 
 /**
  * 
  * 
  * @author Eloy Gomez
  * Indeos Consultoria http://www.indeos.es
  *
  */
 public class ManageRepositories extends CDialog {
 
 	/**
 	 * Descripción de campos
 	 */
 	private CLabel labelName = new CLabel();
 	private CTextField fName;
 	
 	private CLabel labelURI = new CLabel();
 	private CTextField fURI;
 	
 	private Properties ctx;
 	private CButton addBtn;
 	private CButton removeBtn;
 	private OTable table;
 	private RepositoryBeanProvider modelProvider;
 	private BeanTableModel tableModel;
 	
 	private JXTaskPane topPanel = new JXTaskPane();
 	
 	private RepositoryModel selected;
 
 	public ManageRepositories(Properties ctx, Dialog owner) throws HeadlessException {
 		super(owner);
 		this.ctx = ctx;
 		jbInit();
 		pack();
 	}
 
 	private void jbInit()	{
 		setLayout(new BorderLayout(10, 10));
 		
 		topPanel.setTitle(Msg.translate(Env.getCtx(), "URL"));
 		
 		//topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
 		add(topPanel, BorderLayout.NORTH);
 		
 		CPanel formPanel = new CPanel();
 		//formPanel.setLayout(new GridLayout(2, 2, 5, 5));
 		formPanel.setLayout(new GridBagLayout());
 		topPanel.add(formPanel);
 		
 		labelName.setText(Msg.getMsg(ctx, "Name"));	
 		fName = new CTextField(20);
 		labelName.setLabelFor(fName);
 
 		
 		labelURI.setText(Msg.getMsg(ctx, "URL"));
 		fURI = new CTextField(20);
 		labelURI.setLabelFor(fURI);
 		
 		
 		formPanel.add( labelName,new GridBagConstraints( 0,0,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets( 2,2,2,2 ),0,0 ));
 		formPanel.add( fName,new GridBagConstraints( 1,0,1,1,0.3,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets( 2,2,2,20 ),0,0 ));
 		formPanel.add( labelURI,new GridBagConstraints( 0,1,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets( 2,2,2,2 ),0,0 ));
 		formPanel.add( fURI,new GridBagConstraints( 1,1,1,1,0.3,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets( 2,2,2,20 ),0,0 ));
 
 		
 		CPanel topBtnPanel = new CPanel();
 		topPanel.add(topBtnPanel);
 		addBtn = new CButton(Msg.getMsg(ctx, "Add"));
 		addBtn.addActionListener(this);
 		topBtnPanel.add(addBtn);
 		
 		removeBtn = new CButton(Msg.getMsg(ctx, "Remove"));
 		removeBtn.addActionListener(this);
 		topBtnPanel.add(removeBtn);
 		
 		
 		CPanel mainPanel = new CPanel();
 		add(mainPanel, BorderLayout.CENTER);
 		
 		table = new OTable(ctx);
		refreshProvider();
 		CScrollPane scroll = new CScrollPane(table);		
 		mainPanel.add(scroll);
 		
 	}
 		
 	/**
 	 * Get column Definitions
 	 * @return
 	 */
 	public ColumnDefinition[] getColumnDefinitions() {
 		ColumnDefinition[] cols = {				
 				new ColumnDefinition("name", Msg.getMsg(ctx, "Name"), String.class ),
 				new ColumnDefinition("location", Msg.getMsg(ctx, "URL"), String.class ) };
 		return cols;
 	}
 		
 	/* (non-Javadoc)
 	 * @see org.compiere.swing.CDialog#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource().equals(addBtn))	{
 			if (fName.getText() != null && fURI.getText() != null)	{
 				URI uri;
 				try {
 					uri = new URI(fURI.getText());
 				} catch (URISyntaxException e1) {
 					ADialog.error(-1, this, Msg.getMsg(ctx, "Invalid URL"));
 					return;
 				}
 				P2.get().addRepository(uri, fName.getText());
 			}
 		}
 		
 		else if (e.getSource().equals(removeBtn))	{
 			if (selected != null)	{
 				P2.get().removeRepository(selected.getLocation());
 			}
 		}
 		refresh();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.compiere.swing.CDialog#mouseClicked(java.awt.event.MouseEvent)
 	 */
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		int index = table.getSelectedRow();
 		if (index != -1)	{
 			selected = (RepositoryModel) modelProvider.getModel()[index];
 		}
 		
 		refresh();
 	}
 	
 	public void refresh()	{
 		if (selected != null)	{
 			fName.setText(selected.getName());
 			fURI.setText(selected.getLocation().toString());
 		}
 		
		//modelProvider.reload();
		refreshProvider();
 		table.repaint();
 		table.autoSize(false);
 	}
 	
	private void refreshProvider(){
		modelProvider = new RepositoryBeanProvider();
		tableModel = new BeanTableModel(modelProvider, getColumnDefinitions());
		table.setModel(tableModel);
		table.setupTable();
		table.addMouseListener(this);
		table.autoSize(false);
	}
 	
 	
 }
 
 class RepositoryBeanProvider implements IBeanProvider {
 
 	private RepositoryModel[] model;
 	
 	/* (non-Javadoc)
 	 * @see org.opensixen.interfaces.IBeanProvider#getModel()
 	 */
 	@Override
 	public Object[] getModel() {
 		if (model == null)	{
 			load();
 		}
 		
 		return model;
 	}
 
 	private void load()	{
 		model = P2.get().getAllRepositoryModel();
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see org.opensixen.interfaces.IBeanProvider#reload()
 	 */
 	@Override
 	public void reload() {
 		load();		
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opensixen.interfaces.IBeanProvider#getModelClass()
 	 */
 	@Override
 	public Class getModelClass() {
 		return RepositoryModel.class;
 	}
 
 }
 
