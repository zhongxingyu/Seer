 /*
   File: TunablesTestTask.java
 
   Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)
 
   This library is free software; you can redistribute it and/or modify it
   under the terms of the GNU Lesser General Public License as published
   by the Free Software Foundation; either version 2.1 of the License, or
   any later version.
 
   This library is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
   MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
   documentation provided hereunder is on an "as is" basis, and the
   Institute for Systems Biology and the Whitehead Institute
   have no obligations to provide maintenance, support,
   updates, enhancements or modifications.  In no event shall the
   Institute for Systems Biology and the Whitehead Institute
   be liable to any party for direct, indirect, special,
   incidental or consequential damages, including lost profits, arising
   out of the use of this software and its documentation, even if the
   Institute for Systems Biology and the Whitehead Institute
   have been advised of the possibility of such damage.  See
   the GNU Lesser General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License
   along with this library; if not, write to the Free Software Foundation,
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 package org.cytoscape.internal.test.tunables;
 
 
 import java.io.File;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.work.AbstractTask;
 import org.cytoscape.work.Task;
 import org.cytoscape.work.TaskMonitor;
 import org.cytoscape.work.Tunable;
 import org.cytoscape.work.ProvidesGUI;
 import org.cytoscape.work.TunableValidator;
 import org.cytoscape.work.util.BoundedDouble;
 import org.cytoscape.work.util.BoundedFloat;
 import org.cytoscape.work.util.BoundedInteger;
 import org.cytoscape.work.util.BoundedLong;
 import org.cytoscape.work.util.ListMultipleSelection;
 import org.cytoscape.work.util.ListSingleSelection;
 
 
 public class TunablesTestTask extends AbstractTask implements TunableValidator {
 	@Tunable(description="String")
 	public String s;
 
 	@Tunable(description="int")
 	public int i;
 
 	@Tunable(description="Integer")
 	public Integer i2;
 
 	@Tunable(description="long")
 	public long l;
 
 	@Tunable(description="Long")
 	public Long l2;
 
 	@Tunable(description="double")
 	public double d;
 
 	@Tunable(description="Double")
 	public Double d2;
 
 	@Tunable(description="float")
 	public float f;
 
 	@Tunable(description="Float")
 	public Float f2;
 
 	@Tunable(description="File")
 	public File file;
 
 	@Tunable(description="BoundedDouble")
 	public BoundedDouble bd;
 
 	@Tunable(description="boolean")
 	public boolean b;
 
 	@Tunable(description="Boolean")
 	public Boolean b2;
 
 	@Tunable(description="Must be \"valid\"")
 	public String vt;
 
 	private int getterSetterInt;
 
 	public TunablesTestTask() {
 		i2 = Integer.valueOf(1);
 		l2 = Long.valueOf(2L);
 		d2 = Double.valueOf(3.0);
 		f2 = Float.valueOf(4.0f);
 //		file = new File("/");
 		bd = new BoundedDouble(-10.0, 5.0, +10.0, /* lowerStrict = */ true, /* upperStrict = */ true);
 		b2 = Boolean.valueOf(true);
 		getterSetterInt = 22;
 		vt = "";
 	}
 
 	public void run(TaskMonitor e) {
 		System.err.println("String="+s);
 		System.err.println("int="+i);
 		System.err.println("Integer="+i2);
 		System.err.println("long="+l);
 		System.err.println("Long="+l2);
 		System.err.println("double="+d);
 		System.err.println("Double="+d2);
 		System.err.println("float="+f);
 		System.err.println("Float="+f2);
 		System.err.println("File="+file);
 		System.err.println("BoundedDouble="+bd.getValue());
 		System.err.println("boolean="+b);
 		System.err.println("Boolean="+b2);
 		System.err.println("getterSetterInt="+getterSetterInt);
 		System.err.println("Validated tunable="+vt);
 	}
 
 	@Tunable(description="Getter/setter int")
 	public Integer getInt() { return new Integer(getterSetterInt); }
 
 	public void setInt(final Integer newValue) { getterSetterInt = newValue; }
 
	public boolean tunablesAreValid(final Appendable errMsg) {
 		if (vt != null && vt.equals("valid"))
			return true;
 
 		try {
 			errMsg.append("Bad input (" + vt + "): \"valid\" expected!");
 		} finally {
			return false;
 		}
 	}
 
 //	@ProvidesGUI
 	public JPanel getGUI() {
 		final JPanel panel = new JPanel();
 		panel.add(new JLabel("Panel from an @ProvidesGUI-annotated class!"));
 		return panel;
 	}
 }
