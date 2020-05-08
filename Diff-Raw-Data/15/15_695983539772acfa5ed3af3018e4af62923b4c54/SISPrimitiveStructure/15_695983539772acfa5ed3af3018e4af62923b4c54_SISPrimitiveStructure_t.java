 package org.iucn.sis.shared.api.structures;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.models.PrimitiveField;
 
 public abstract class SISPrimitiveStructure<T> extends Structure<PrimitiveField<T>> {
 
 	public SISPrimitiveStructure(String struct, String descript, String structID) {
 		this(struct, descript, structID, null);
 	}
 
 	public SISPrimitiveStructure(String struct, String descript, String structID, Object data) {
 		super(struct, descript, structID, data);
 	}
 	
 	/**
 	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
 	 * if it contains multiples structures, all of those, in order.
 	 */
 	@Override
 	public ArrayList<String> extractDescriptions() {
 		ArrayList<String> ret = new ArrayList<String>();
 		ret.add(description);
 		return ret;
 	}
 	
 	/**
 	 * Compares the data this structure was set with, with what it gets from its widget(s).
 	 * Returns true if they differ.
 	 * @return true or false
 	 */
 	public boolean hasChanged(PrimitiveField<T> field) {
 		String oldValue = field == null ? null : field.getRawValue();
 		if ("".equals(oldValue))
 			oldValue = null;
 		
 		String newValue = getData();
 		if ("".equals(newValue))
 			newValue = null;
 		
 		Debug.println("Comparing {0} to {1}", oldValue, newValue);
 		
 		if (newValue == null)
 			return oldValue != null;
 		else
 			if (oldValue == null)
 				return true;
 			else
 				return !newValue.equals(oldValue);
 	}
 	
 	public boolean isPrimitive() {
 		return true;
 	}
 	
 	@Override
 	public void save(Field parent, PrimitiveField<T> field) {
 		final String data = getData();
 		Debug.println("Saving data for %s: %s", getId(), data);
 		if (data != null) {
 			if (field == null) {
 				field = getNewPrimitiveField();
 				field.setField(parent);
 				parent.addPrimitiveField(field);
 			}
 			field.setRawValue(data);
 		}
		else {
			if (field != null) {
				field.setRawValue("");
				if (parent != null) {
					Debug.println("Remvoing & clearing existing value %s for %s", field.getRawValue(), getId());
					parent.getPrimitiveField().remove(field);
				}
				else
					Debug.println("Only clearing existing value %s for %s", field.getRawValue(), getId());
			}
			else
				Debug.println("Skipping %s, no data to save", getId());
		}
 	}
 	
 	@Override
 	public List<ClassificationInfo> getClassificationInfo() {
 		final ArrayList<ClassificationInfo> list = new ArrayList<ClassificationInfo>();
 		list.add(new ClassificationInfo(description, getData()));
 		return list;
 	}
 	
 	/**
 	 * Returns an empty PrimitiveField object, typed properly for this PrimitiveFieldWidget's
 	 * data type.
 	 * 
 	 * @return a PrimitiveField object
 	 */
 	protected abstract PrimitiveField<T> getNewPrimitiveField();
 
 }
