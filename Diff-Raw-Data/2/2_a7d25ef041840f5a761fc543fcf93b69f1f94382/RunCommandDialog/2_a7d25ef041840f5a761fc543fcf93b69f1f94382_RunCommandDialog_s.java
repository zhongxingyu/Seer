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
 
 package org.opensixen.dev.omvc.swing;
 
 import java.awt.BorderLayout;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.HeadlessException;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 
 import javax.management.RuntimeErrorException;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 
 import org.adempiere.plaf.AdempierePLAF;
 import org.compiere.apps.AEnv;
 import org.compiere.apps.ConfirmPanel;
 import org.compiere.swing.CButton;
 import org.compiere.swing.CDialog;
 import org.compiere.swing.CLabel;
 import org.compiere.swing.CPanel;
 import org.compiere.swing.CTextField;
 import org.compiere.util.Env;
 import org.compiere.util.Msg;
 import org.opensixen.osgi.Service;
 import org.opensixen.osgi.ServiceQuery;
 import org.opensixen.osgi.interfaces.ICommand;
 
 /**
  * 
  * 
  * 
  * @author Eloy Gomez
  * Indeos Consultoria http://www.indeos.es
  *
  */
 public class RunCommandDialog extends CDialog {
 
 	/**
 	 * Descripcion de campos
 	 */
 	
 	private CTextField fCmd;
 	private CLabel header;
 	
 	//Paneles
 	private CPanel mainPanel;
 	private CPanel centerPanel;
 	private ConfirmPanel confirm = new ConfirmPanel(true);
 
 
 	public RunCommandDialog(Frame owner) throws HeadlessException {
 		super(owner);
 		jbInit();
 		AEnv.positionCenterWindow(owner, this);
 	}
 	
 	
 	private void jbInit()	{
 		centerPanel = new CPanel();
 		centerPanel.setLayout(new GridBagLayout());
 		
 		mainPanel = new CPanel();
 		mainPanel.setLayout(new BorderLayout());
 		
 		getContentPane().add(mainPanel);
 
 		mainPanel.add(centerPanel,BorderLayout.CENTER);
 		mainPanel.add(confirm,BorderLayout.SOUTH);
 		
 		centerPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
 		
 		Font font = AdempierePLAF.getFont_Field().deriveFont(18f);
 		header = new CLabel(Msg.translate(Env.getCtx(), "System Command"));
 		header.setFontBold(true);
 		header.setFont(font);
 		
 		fCmd = new CTextField(50);
 		
 		centerPanel.add( header,new GridBagConstraints( 1,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets( 2,2,20,2 ),0,0 ));
 
 		centerPanel.add( new CLabel(Msg.getMsg(Env.getCtx(), "Command")),new GridBagConstraints( 0,1,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets( 2,2,2,2 ),0,0 ));
 		centerPanel.add( fCmd,new GridBagConstraints( 1,1,1,1,0.3,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets( 2,2,2,2 ),0,0 ));	
 		
 		confirm.addActionListener(this);
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals(ConfirmPanel.A_CANCEL))	{
 			dispose();
 		}
		if (e.getSource().equals(ConfirmPanel.A_OK))	{
 			run();
 		}
 	}
 
 
 	/**
 	 * 
 	 */
 	private void run() {
 		String name = fCmd.getText();
 		if (name == null || name.length() == 0)	{
 			return;
 		}
 		ServiceQuery query = new ServiceQuery(ICommand.P_NAME, name);
 		ICommand command = 	Service.locate(ICommand.class, query);	
 		if (command == null )	{
 			return;
 		}
 		command.prepare();
 		try {
 			command.doIt();
 		}
 		catch (Exception e)	{
 			throw new RuntimeException(e);
 		}
 		
 	}
 
 }
