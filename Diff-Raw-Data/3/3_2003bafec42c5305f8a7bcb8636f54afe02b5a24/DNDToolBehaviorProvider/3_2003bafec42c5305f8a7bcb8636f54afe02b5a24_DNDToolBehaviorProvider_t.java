 package edu.teco.dnd.graphiti;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import edu.teco.dnd.graphiti.model.FunctionBlockModel;
 import edu.teco.dnd.graphiti.model.InputModel;
 import edu.teco.dnd.graphiti.model.OutputModel;
 
 import org.eclipse.graphiti.features.ICreateFeature;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Text;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.palette.IPaletteCompartmentEntry;
 import org.eclipse.graphiti.palette.impl.ConnectionCreationToolEntry;
 import org.eclipse.graphiti.palette.impl.ObjectCreationToolEntry;
 import org.eclipse.graphiti.palette.impl.PaletteCompartmentEntry;
 import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
 
 /**
  * Provides the palette and the selection border.
  */
 public class DNDToolBehaviorProvider extends DefaultToolBehaviorProvider {
 	/**
 	 * Passes the diagram type to the super constructor.
 	 * 
 	 * @param diagramTypeProvider
 	 *            the diagram type this tool behavior provider belongs to
 	 */
 	public DNDToolBehaviorProvider(final DNDDiagramTypeProvider diagramTypeProvider) {
 		super(diagramTypeProvider);
 	}
 
 	/**
 	 * Returns the palette entries to show.
 	 * 
 	 * @return the palette entries to show
 	 */
 	@Override
 	public IPaletteCompartmentEntry[] getPalette() {
 		List<IPaletteCompartmentEntry> palette = new ArrayList<IPaletteCompartmentEntry>();
 		PaletteCompartmentEntry connections = new PaletteCompartmentEntry("Connections", null);
 		palette.add(connections);
 		DNDCreateDataConnectionFeature dataConnectionFeature = new DNDCreateDataConnectionFeature(
 				(DNDFeatureProvider) getFeatureProvider());
 		ConnectionCreationToolEntry connectionCreationToolEntry = new ConnectionCreationToolEntry(
 				dataConnectionFeature.getName(), dataConnectionFeature.getDescription(), null, null);
 		connections.addToolEntry(connectionCreationToolEntry);
 		connectionCreationToolEntry.addCreateConnectionFeature(dataConnectionFeature);
 		Map<String, List<ICreateFeature>> categories = new HashMap<String, List<ICreateFeature>>();
 		for (ICreateFeature cf : getFeatureProvider().getCreateFeatures()) {
 			String category = "Other";
 			if (cf instanceof DNDCreateBlockFeature) {
 				DNDCreateBlockFeature cbf = (DNDCreateBlockFeature) cf;
 				BlockType blockType = cbf.getBlockType().getAnnotation(BlockType.class);
 				if (blockType != null) {
 					category = blockType.value();
 				}
 				if (!categories.containsKey(category)) {
 					categories.put(category, new ArrayList<ICreateFeature>());
 				}
 			}
 			categories.get(category).add(cf);
 		}
 		List<String> categoryList = new ArrayList<String>(categories.keySet());
 		Collections.sort(categoryList);
 		for (String category : categoryList) {
 			PaletteCompartmentEntry pce = new PaletteCompartmentEntry(category, null);
 			List<ICreateFeature> cfs = categories.get(category);
 			Collections.sort(cfs, new Comparator<ICreateFeature>() {
 				@Override
 				public int compare(final ICreateFeature o1, final ICreateFeature o2) {
 					return o1.getName().compareTo(o2.getName());
 				}
 			});
 			for (ICreateFeature cf : cfs) {
 				pce.addToolEntry(new ObjectCreationToolEntry(cf.getName(), cf.getDescription(), null, null,
 						cf));
 			}
 			palette.add(pce);
 		}
 		return palette.toArray(new IPaletteCompartmentEntry[0]);
 	}
 
 	@Override
 	public String getToolTip(final GraphicsAlgorithm ga) {
 		PictogramElement pe = ga.getPictogramElement();
 		Object bo = getFeatureProvider().getBusinessObjectForPictogramElement(pe);
 		String name = null;
 		if (bo instanceof FunctionBlockModel) {
 			if (ga instanceof Text) {
 				name = ((Text) ga).getValue();
 			} else {
 				name = ((FunctionBlockModel) bo).getTypeName();
 			}
 		} else if (bo instanceof OutputModel) {
 			OutputModel output = (OutputModel) bo;
 			name = output.getName() + " (" + simplifyName(output.getType()) + ")";
 		} else if (bo instanceof InputModel) {
 			InputModel input = (InputModel) bo;
 			name = input.getName() + " (" + simplifyName(input.getType()) + ")";
 		} else if (ga instanceof Text) {
 			name = ((Text) ga).getValue();
 		}
 		if (name != null && !name.isEmpty()) {
 			return name;
 		}
		final Object superTip = super.getToolTip(ga);
		return superTip instanceof String ? (String) superTip : "";
 	}
 
 	/**
 	 * Simplyfies a name by cutting off parts of it.
 	 * 
 	 * @param name
 	 *            name to simplify
 	 * @return simplified name
 	 */
 	private static String simplifyName(final String name) {
 		if (name == null) {
 			return null;
 		}
 		return name.substring(name.lastIndexOf('.') + 1);
 	}
 }
