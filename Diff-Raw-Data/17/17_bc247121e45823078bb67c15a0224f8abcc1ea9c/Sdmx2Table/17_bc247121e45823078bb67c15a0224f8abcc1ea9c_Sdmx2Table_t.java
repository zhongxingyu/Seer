 package org.virtual.sdmxregistry.transforms;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.namespace.QName;
 
 import org.sdmxsource.sdmx.api.model.beans.base.AnnotationBean;
 import org.sdmxsource.sdmx.api.model.beans.base.TextTypeWrapper;
 import org.sdmxsource.sdmx.api.model.beans.codelist.CodeBean;
 import org.sdmxsource.sdmx.api.model.beans.codelist.CodelistBean;
 import org.virtualrepository.sdmx.SdmxCodelist;
 import org.virtualrepository.spi.Transform;
 import org.virtualrepository.tabular.Column;
 import org.virtualrepository.tabular.DefaultTable;
 import org.virtualrepository.tabular.Row;
 import org.virtualrepository.tabular.Table;
 
 public class Sdmx2Table implements Transform<SdmxCodelist, CodelistBean, Table> {
 
 	@Override
 	public Table apply(SdmxCodelist asset, CodelistBean codelist) throws Exception {
 		
 		Set<Column> columns = new LinkedHashSet<Column>(); //keeps insertion order
		
 		List<Row> rows = new ArrayList<Row>();
 		
		Column codeColumn = new Column(new QName(asset.name()+"-code"), new QName("code"), String.class);
		
		//add code column
		if (!codelist.getItems().isEmpty())
			columns.add(codeColumn);
		
 		for (CodeBean code : codelist.getItems()) {
 			
 			Map<QName,String> data = new HashMap<QName, String>();
 			
			data.put(codeColumn.name(),code.getId());
			
			//add name columns
 			for (TextTypeWrapper name: code.getNames()) {
 				QName columnName = new QName("name-"+name.getLocale());
 				Column column = new Column(columnName, new QName("name"), String.class);
 				columns.add(column);
 				data.put(columnName,name.getValue());
 			}
 			
			//add description columns
 			for (TextTypeWrapper description: code.getDescriptions()) {
 				QName columnName = new QName("description-"+description.getLocale());
 				Column column = new Column(columnName, new QName("description"), String.class);
 				columns.add(column);
 				data.put(columnName,description.getValue());
 			}
 			
			//add annotation columns
 			for (AnnotationBean annotation :  code.getAnnotations())
 				for (TextTypeWrapper text: annotation.getText()) {
 					String title = annotation.getTitle()==null?
 								   			annotation.getType()==null?"annotation":annotation.getType()
 							       :annotation.getTitle();
 					QName columnName = new QName(title+"-"+text.getLocale());
 					QName type = annotation.getType()==null?new QName("annotation"):new QName(annotation.getType());
 					Column column = new Column(columnName, type, String.class);
 					columns.add(column);
 					data.put(columnName,text.getValue());
 				}
 			
 			Row row = new Row(data);
 			rows.add(row);
 		}		
 
 		return new DefaultTable(new ArrayList<Column>(columns), rows);
 	}
 
 	@Override
 	public Class<CodelistBean> inputAPI() {
 		return CodelistBean.class;
 	}
 
 	@Override
 	public Class<Table> outputAPI() {
 		return Table.class;
 	}
 
 	
 }
