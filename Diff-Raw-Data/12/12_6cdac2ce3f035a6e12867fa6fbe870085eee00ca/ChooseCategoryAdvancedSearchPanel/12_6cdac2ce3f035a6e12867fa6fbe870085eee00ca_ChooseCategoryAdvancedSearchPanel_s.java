 package edu.wustl.cab2b.client.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 
 /**
  * The actual implementation of the {@link AbstractAdvancedSearchPanel} as is
  * required in the choose category section from the main search dialog.
  * 
  * @author mahesh_iyer/chetan_bh
  * 
  */
 
 public class ChooseCategoryAdvancedSearchPanel extends AbstractAdvancedSearchPanel  
 {
 	/**
 	 * Constructor that just calls the super class version.
 	 *
 	 */
 	public ChooseCategoryAdvancedSearchPanel()
 	{
 		super();		
 	}
 		
 	/**
 	 * The abstract method implementation from the base class that adds
 	 * components in a way required by this implementation of the
 	 * {@link AbstractAdvancedSearchPanel}
 	 * 
 	 */		
 	protected void addComponents()
 	{	
 		m_taskPane.setLayout(new RiverLayout());
 		m_taskPane.getContentPane().setBackground(Color.WHITE);
 		//Add all the componenets as required by this panel.
 		m_taskPane.add(m_chkClass);
 		m_taskPane.add("tab tab ", m_chkClassDesc);
 		m_taskPane.add("br", m_chkAttribute);
 	//	m_taskPane.add("tab",m_chkClassDef);
 		m_taskPane.add("tab tab ",m_chkPermissibleValues);
 		/**
 		 * Dummy labels to set place properly
 		 */
		for(int i = 0 ;i<30;i++)
 		{
 			m_taskPane.add("tab", new Cab2bLabel(""));
 		}
 		m_taskPane.add("br", m_radioText);
 		m_taskPane.add(m_radioConceptCode);
		m_taskPane.setPreferredSize(new Dimension(450,150));
 	}	
 }
