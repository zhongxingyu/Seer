 package org.geworkbench.bison.model.analysis;
 
 import javax.swing.JPanel;
 
 /**
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Company: First Genetic Trust Inc.</p>
  * <p/>
  * 
  * Marker base class.
  *
  * @author First Genetic Trust
  * @version $Id$
  */
 public class ParameterPanel extends JPanel {
 	private static final long serialVersionUID = 510957897262336390L;
   
	private boolean hasParametersToSave = true;
	
	
	public boolean hasParametersToSave()
	{
		return hasParametersToSave;
	}
	
	public void hasParametersToSave(boolean flag)
	{
		hasParametersToSave = flag;
	}
	
 
 }
