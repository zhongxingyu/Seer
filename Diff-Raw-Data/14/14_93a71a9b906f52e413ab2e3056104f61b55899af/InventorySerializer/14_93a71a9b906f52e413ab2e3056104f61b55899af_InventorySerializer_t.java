 package ru.redsolution.bst.data.serializer;
 
 import java.io.IOException;
 
 import org.xmlpull.v1.XmlSerializer;
 
 import ru.redsolution.bst.data.BST;
 import ru.redsolution.bst.data.tables.SelectedTable;
 import android.database.Cursor;
 
 public class InventorySerializer extends BaseSerializer {
 
 	@Override
 	protected String getContainerName() {
 		return "inventory";
 	}
 
 	@Override
 	protected void renderContainerAttrs(XmlSerializer serializer)
 			throws IllegalArgumentException, IllegalStateException, IOException {
 		String myCompany = BST.getInstance().getSelectedMyCompany();
 		String warehouse = BST.getInstance().getSelectedWarehouse();
		serializer.attribute("", "applicable", "true");
		serializer.attribute("", "payerVat", "true");
		serializer.attribute("", "vatIncluded", "true");
 		serializer.attribute("", "targetRequisiteId", myCompany);
 		serializer.attribute("", "sourceRequisiteId", myCompany);
		serializer.attribute("", "sourceStoreId", warehouse);
 		serializer.attribute("", "sourceAgentId", myCompany);
 		serializer.attribute("", "targetAgentId", myCompany);
 	}
 
 	@Override
 	protected String getItemName() {
 		return "inventoryPosition";
 	}
 
 	@Override
 	protected void renderItemAttrs(XmlSerializer serializer, Cursor cursor)
 			throws IllegalArgumentException, IllegalStateException, IOException {
 		String good = cursor.getString(cursor
 				.getColumnIndex(SelectedTable.Fields._ID));
 		int quantity = cursor.getInt(cursor
 				.getColumnIndex(SelectedTable.Fields.QUANTITY));
 		serializer.attribute("", "quantity", String.valueOf(quantity));
 		serializer.attribute("", "goodId", good);
 		serializer.attribute("", "correctionAmount", String.valueOf(quantity));
 	}
 
 }
