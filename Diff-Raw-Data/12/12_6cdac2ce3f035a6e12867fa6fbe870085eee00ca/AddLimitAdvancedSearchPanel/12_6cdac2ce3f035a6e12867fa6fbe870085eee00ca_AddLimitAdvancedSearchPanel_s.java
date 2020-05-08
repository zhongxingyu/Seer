 package edu.wustl.cab2b.client.ui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import org.jdesktop.swingx.VerticalLayout;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * The actual implementation of the {@link AbstractAdvancedSearchPanel} as is
  * required in the AddLimit section from the main search dialog.
  * 
  * @author mahesh_iyer/chetan_bh
  * 
  */
 
 public class AddLimitAdvancedSearchPanel extends AbstractAdvancedSearchPanel {
 	/**
 	 * Constructor that invokes the base class version.
 	 */
 	AddLimitAdvancedSearchPanel() {
 		super();
 		this.setLayout(new VerticalLayout(0));
 		Logger.out.debug("AddLimitAdvancedSearchPanel : VerticalLayout set.");
 	}
 
 	/**
 	 * The abstract method implementation from the base class that adds
 	 * components in a way required by this implementation of the
 	 * {@link AbstractAdvancedSearchPanel}
 	 * 
 	 */
 	protected void addComponents() {
 		m_taskPane.setLayout(new RiverLayout(0, 5));
 		m_taskPane.getContentPane().setBackground(Color.WHITE);
 		// Add all the componenets as required by this panel.
 		m_taskPane.add(m_chkClass);
 		m_taskPane.add("tab", m_chkClassDesc);
 		m_taskPane.add("br", m_chkAttribute);
 		m_taskPane.add("tab", m_chkPermissibleValues);
 
 		m_taskPane.add("br", m_radioText);
 		m_taskPane.add(m_radioConceptCode);
		m_taskPane.setPreferredSize(new Dimension(222, 125));
 
 	}
 }
