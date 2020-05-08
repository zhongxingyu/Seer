 package org.dawb.workbench.plotting.system.dialog;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.workbench.plotting.system.swtxy.XYRegionGraph;
 import org.dawb.workbench.plotting.system.swtxy.selection.AbstractSelectionRegion;
 import org.eclipse.jface.preference.ColorSelector;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.Text;
 
 public class RegionComposite extends Composite {
 
 	private XYRegionGraph xyGraph;
 
 	private Text   nameText;
 	private CCombo regionType;
 	private CCombo xCombo, yCombo;
 	private Button showPoints;
 	private AbstractSelectionRegion editingRegion;
 	private ColorSelector colorSelector;
 	private Spinner alpha;
 	private Button motile;
 	private Button visible;
 	private Button showLabel;
 
 	public RegionComposite(final Composite parent, final int style, final XYRegionGraph xyGraph, final RegionType defaultRegion) {
 		
 		super(parent, SWT.NONE);
 		this.xyGraph = xyGraph;
 		
 		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		setLayout(new org.eclipse.swt.layout.GridLayout(2, false));
 
 		final Label nameLabel = new Label(this, SWT.NONE);
 		nameLabel.setText("Name   ");
 		nameLabel.setLayoutData(new GridData());
 
 		nameText = new Text(this, SWT.BORDER | SWT.SINGLE);
 		nameText.setToolTipText("Region name");
 		nameText.setLayoutData(new GridData(SWT.FILL, 0, true, false));
 		
 		final Label horiz = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
 		horiz.setLayoutData(new GridData(SWT.FILL, 0, true, false, 2, 1));
 
 		final Label typeLabel = new Label(this, SWT.NONE);
 		typeLabel.setText("Type");
 		typeLabel.setLayoutData(new GridData());
 
 		regionType = new CCombo(this, SWT.NONE);
 		regionType.setEditable(false);
 		regionType.setToolTipText("Region type");
 		regionType.setLayoutData(new GridData(SWT.FILL, 0, true, false));
 		for (RegionType type : RegionType.ALL_TYPES) {
 			regionType.add(type.getName());
 		}
 		regionType.select(defaultRegion.getIndex());
 
 		xCombo = createAxisChooser(this, "X Axis", xyGraph.getXAxisList());	
 		yCombo = createAxisChooser(this, "Y Axis", xyGraph.getYAxisList());	
 		
 		Label colorLabel = new Label(this, 0);
 		colorLabel.setText("Selection Color");		
 		colorLabel.setLayoutData(new GridData());
 		
 		colorSelector = new ColorSelector(this);
 		colorSelector.getButton().setLayoutData(new GridData());		
 		colorSelector.setColorValue(defaultRegion.getDefaultColor().getRGB());
 		
 		
 		final Label horiz2 = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
 		horiz2.setLayoutData(new GridData(SWT.FILL, 0, true, false, 2, 1));
 
 		Label alphaLabel = new Label(this, 0);
 		alphaLabel.setText("Alpha level");		
 		alphaLabel.setLayoutData(new GridData());
 		
 		alpha = new Spinner(this, SWT.NONE);
 		alpha.setToolTipText("Alpha transparency level of the shaded shape");
 		alpha.setLayoutData(new GridData());		
 		alpha.setMinimum(0);
 		alpha.setMaximum(255);
 		alpha.setSelection(80);
 
 		this.motile = new Button(this, SWT.CHECK);
		motile.setText("   Motile   ");		
 		motile.setToolTipText("When true, this selection can be resized and moved around the graph.");
 		motile.setLayoutData(new GridData(0, 0, false, false, 2, 1));
 		motile.setSelection(true);
 		
 		
 		this.showPoints = new Button(this, SWT.CHECK);
 		showPoints.setText("   Show vertex values");		
 		showPoints.setToolTipText("When on this will show the actual value of the point in the axes it was added.");
 		showPoints.setLayoutData(new GridData(0, 0, false, false, 2, 1));
 
 		this.visible = new Button(this, SWT.CHECK);
 		visible.setText("   Show region");		
 		visible.setToolTipText("You may turn off the visibility of this region.");
 		visible.setLayoutData(new GridData(0, 0, false, false, 2, 1));
 		visible.setSelection(true);
 		
 		
 		this.showLabel = new Button(this, SWT.CHECK);
 		showLabel.setText("   Show name");		
 		showLabel.setToolTipText("Turn on to show the name of a selection region.");
 		showLabel.setLayoutData(new GridData(0, 0, false, false, 2, 1));
 		showLabel.setSelection(true);
 		
 
 		// Should be last
 		nameText.setText(getDefaultName(defaultRegion.getIndex()));
 
 	}
 
 	private CCombo createAxisChooser(RegionComposite parent,
 			                         String label, List<Axis> xAxisList) {
 		
 		final Label xAxisLabel = new Label(parent, SWT.NONE);
 		xAxisLabel.setText(label);
 		xAxisLabel.setLayoutData(new GridData());
 
 		CCombo combo = new CCombo(this, SWT.NONE);
 		combo.setEditable(false);
 		combo.setToolTipText("Existing axis on the graph ");
 		combo.setLayoutData(new GridData(SWT.FILL, 0, true, false));
 
 		for(Axis axis : xAxisList)  combo.add(axis.getTitle());
 		
 		combo.select(0);		
 		
 		return combo;
 	}
 
 	private static Map<Integer,Integer> countMap;
 	private String getDefaultName(int sel) {
 		if (countMap==null) countMap = new HashMap<Integer,Integer>(5);
 		if (!countMap.containsKey(sel)) {
 			countMap.put(sel, 0);
 		}
 		int count = countMap.get(sel);
 		count++;
 		return regionType.getItem(sel)+" "+count;
 	}
 
 
 	public AbstractSelectionRegion createRegion() throws Exception {
 		
 		final Axis xAxis = xyGraph.getXAxisList().get(xCombo.getSelectionIndex());
 		final Axis yAxis = xyGraph.getYAxisList().get(yCombo.getSelectionIndex());
 		
 		AbstractSelectionRegion region=null;
 		
 		final String txt = nameText.getText();
 		final Pattern pattern = Pattern.compile(".* (\\d+)");
 		final Matcher matcher = pattern.matcher(txt);
 		if (matcher.matches()) {
 			final int count = Integer.parseInt(matcher.group(1));
 			countMap.put(regionType.getSelectionIndex(), count);
 		}
 		
 		region = xyGraph.createRegion(txt, xAxis, yAxis, RegionType.getRegion(regionType.getSelectionIndex()), true);
 		
 		this.editingRegion = region;
 		getEditingRegion();
 		
         return region;
 	}
 	
 	
 	public void setEditingRegion(AbstractSelectionRegion region) {
         this.editingRegion = region;
         
         nameText.setText(region.getName());
 		regionType.select(region.getRegionType().getIndex());
 		regionType.setEnabled(false);
 		regionType.setEditable(false);
 		
 		int index = xyGraph.getXAxisList().indexOf(region.getxAxis());
 		xCombo.select(index);
 		
 		index = xyGraph.getYAxisList().indexOf(region.getyAxis());
 		yCombo.select(index);
 		
 		colorSelector.setColorValue(region.getRegionColor().getRGB());
 		alpha.setSelection(region.getAlpha());
 		motile.setSelection(region.isMobile());
 		showPoints.setSelection(region.isShowPosition());
 		visible.setSelection(region.isVisible());
 		showLabel.setSelection(region.isShowLabel());
 	}
 	
 	public AbstractSelectionRegion getEditingRegion() {
 		
 		final String txt = nameText.getText();
 		editingRegion.setName(txt);
 		editingRegion.setxAxis(xyGraph.getXAxisList().get(xCombo.getSelectionIndex()));
 		editingRegion.setyAxis(xyGraph.getYAxisList().get(yCombo.getSelectionIndex()));
 		editingRegion.setShowPosition(showPoints.getSelection());
 		editingRegion.setRegionColor(new Color(getDisplay(), colorSelector.getColorValue()));
 		editingRegion.setAlpha(alpha.getSelection());
 		editingRegion.setMobile(motile.getSelection());
 		editingRegion.setVisible(visible.getSelection());
 		editingRegion.setShowLabel(showLabel.getSelection());
         return editingRegion;
 	}
 
 
 	public void applyChanges() {
 		AbstractSelectionRegion region = getEditingRegion();
 		region.repaint();
 	}
 
 	public void disposeRegion(AbstractSelectionRegion region) {
 		 xyGraph.disposeRegion(region);
 	}
 }
