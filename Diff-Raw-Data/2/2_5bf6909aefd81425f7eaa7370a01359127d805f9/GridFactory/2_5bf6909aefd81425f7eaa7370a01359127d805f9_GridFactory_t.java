 /*
  * Copyright 2009 Sysmap Solutions Software e Consultoria Ltda.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package br.com.sysmap.crux.widgets.rebind.grid;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import br.com.sysmap.crux.core.client.datasource.PagedDataSource;
 import br.com.sysmap.crux.core.client.utils.EscapeUtils;
 import br.com.sysmap.crux.core.client.utils.StringUtils;
 import br.com.sysmap.crux.core.i18n.MessagesFactory;
 import br.com.sysmap.crux.core.rebind.CruxGeneratorException;
 import br.com.sysmap.crux.core.rebind.datasource.DataSources;
 import br.com.sysmap.crux.core.rebind.formatter.Formatters;
 import br.com.sysmap.crux.core.rebind.screen.widget.AttributeProcessor;
 import br.com.sysmap.crux.core.rebind.screen.widget.ViewFactoryCreator.SourcePrinter;
 import br.com.sysmap.crux.core.rebind.screen.widget.WidgetCreator;
 import br.com.sysmap.crux.core.rebind.screen.widget.WidgetCreatorContext;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.align.AlignmentAttributeParser;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.align.HorizontalAlignment;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.align.VerticalAlignment;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.children.AnyWidgetChildProcessor;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.children.ChoiceChildProcessor;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.children.WidgetChildProcessor;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.DeclarativeFactory;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagAttribute;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagAttributeDeclaration;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagAttributes;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagAttributesDeclaration;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagChild;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagConstraints;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagChildren;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagEvent;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagEvents;
 import br.com.sysmap.crux.core.utils.ClassUtils;
 import br.com.sysmap.crux.widgets.client.grid.ColumnDefinition;
 import br.com.sysmap.crux.widgets.client.grid.ColumnDefinitions;
 import br.com.sysmap.crux.widgets.client.grid.DataColumnDefinition;
 import br.com.sysmap.crux.widgets.client.grid.Grid;
 import br.com.sysmap.crux.widgets.client.grid.Grid.SortingType;
 import br.com.sysmap.crux.widgets.client.grid.RowSelectionModel;
 import br.com.sysmap.crux.widgets.client.grid.WidgetColumnDefinition;
 import br.com.sysmap.crux.widgets.client.grid.WidgetColumnDefinition.WidgetColumnCreator;
 import br.com.sysmap.crux.widgets.rebind.WidgetGeneratorMessages;
 import br.com.sysmap.crux.widgets.rebind.event.RowEventsBind.BeforeRowSelectEvtBind;
 import br.com.sysmap.crux.widgets.rebind.event.RowEventsBind.RowClickEvtBind;
 import br.com.sysmap.crux.widgets.rebind.event.RowEventsBind.RowDoubleClickEvtBind;
 import br.com.sysmap.crux.widgets.rebind.event.RowEventsBind.RowRenderEvtBind;
 
 import com.google.gwt.core.ext.typeinfo.JClassType;
 import com.google.gwt.core.ext.typeinfo.JType;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 
 /**
  * @author Gesse S. F. Dafe
  */
 @DeclarativeFactory(id="grid", library="widgets", targetWidget=Grid.class)
 @TagAttributesDeclaration({
 	@TagAttributeDeclaration(value="pageSize", type=Integer.class, defaultValue="8"),
 	@TagAttributeDeclaration(value="rowSelection", type=RowSelectionModel.class, defaultValue="unselectable"),
 	@TagAttributeDeclaration(value="cellSpacing", type=Integer.class, defaultValue="1"),
 	@TagAttributeDeclaration(value="autoLoadData", type=Boolean.class, defaultValue="false"),
 	@TagAttributeDeclaration(value="stretchColumns", type=Boolean.class, defaultValue="false"),
 	@TagAttributeDeclaration(value="highlightRowOnMouseOver", type=Boolean.class, defaultValue="false"),
 	@TagAttributeDeclaration(value="fixedCellSize", type=Boolean.class, defaultValue="false"),
 	@TagAttributeDeclaration(value="emptyDataFilling", type=String.class, defaultValue=" "),
 	@TagAttributeDeclaration(value="defaultSortingColumn", type=String.class),
 	@TagAttributeDeclaration(value="defaultSortingType", type=SortingType.class, defaultValue="ascending")
 })
 @TagAttributes({
 	@TagAttribute(value="dataSource", processor=GridFactory.DataSourceAttributeParser.class)
 })
 @TagEvents({
 	@TagEvent(RowClickEvtBind.class),
 	@TagEvent(RowDoubleClickEvtBind.class),
 	@TagEvent(RowRenderEvtBind.class),
 	@TagEvent(BeforeRowSelectEvtBind.class)
 })
 @TagChildren({
 	@TagChild(value=GridFactory.ColumnProcessor.class, autoProcess=false)
 })
 public class GridFactory extends WidgetCreator<WidgetCreatorContext>
 {
 	protected static WidgetGeneratorMessages widgetMessages = (WidgetGeneratorMessages)MessagesFactory.getMessages(WidgetGeneratorMessages.class);
 
 	public String instantiateWidget(SourcePrinter out, JSONObject metaElem, String widgetId) throws CruxGeneratorException
 	{
 		String varName = createVariableName("widget");
 		String className = getWidgetClassName();
 		String columnsDefinitions = getColumnDefinitions(out, metaElem);
 		
 		out.println(className + " " + varName+" = new "+className+"("+columnsDefinitions+", "+getPageSize(metaElem)+", "+
                 getRowSelectionModel(metaElem)+", "+getCellSpacing(metaElem)+", "+getAutoLoad(metaElem)+", "+
                 getStretchColumns(metaElem)+", "+getHighlightRowOnMouseOver(metaElem)+", "+
                 getEmptyDataFilling(metaElem)+", "+isFixedCellSize(metaElem)+", "+getSortingColumn(metaElem)+", "+getSortingType(metaElem)+");");
 		
 		return varName;
 	}
 	
 	/**
 	 * @param gridElem
 	 * @return
 	 */
 	private String getSortingType(JSONObject gridElem)
 	{
 		String sortingType = gridElem.optString("defaultSortingType");
 		if(!StringUtils.isEmpty(sortingType))
 		{
 			SortingType sort = SortingType.valueOf(sortingType);
 			return SortingType.class.getCanonicalName()+"."+sort.toString();
 		}
 		return null;
 	}
 
 	/**
 	 * @param gridElem
 	 * @return
 	 */
 	private String getSortingColumn(JSONObject gridElem)
 	{
 		String sort = gridElem.optString("defaultSortingColumn");
 		if (!StringUtils.isEmpty(sort))
 		{
 			return EscapeUtils.quote(sort);
 		}
 		return null;
 	}
 
 	/**
 	 * @param gridElem
 	 * @return
 	 */
 	private boolean isFixedCellSize(JSONObject gridElem)
 	{
 		String fixedCellSize = gridElem.optString("fixedCellSize");
 		
 		if(fixedCellSize != null && fixedCellSize.trim().length() > 0)
 		{
 			return Boolean.parseBoolean(fixedCellSize);
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * @param gridElem
 	 * @return
 	 */
 	private String getEmptyDataFilling(JSONObject gridElem)
 	{
 		String emptyDataFilling = gridElem.optString("emptyDataFilling");
 		
 		if(emptyDataFilling != null && emptyDataFilling.trim().length() > 0)
 		{
 			return EscapeUtils.quote(emptyDataFilling);
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * @param gridElem
 	 * @return
 	 */
 	private boolean getHighlightRowOnMouseOver(JSONObject gridElem)
 	{
 		String highlight = gridElem.optString("highlightRowOnMouseOver");
 		
 		if(highlight != null && highlight.trim().length() > 0)
 		{
 			return Boolean.parseBoolean(highlight);
 		}
 		
 		return false;
 	}
 
 	/**
 	 * @param gridElem
 	 * @return
 	 */
 	private boolean getAutoLoad(JSONObject gridElem)
 	{
 		String autoLoad = gridElem.optString("autoLoadData");
 		
 		if(autoLoad != null && autoLoad.trim().length() > 0)
 		{
 			return Boolean.parseBoolean(autoLoad);
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * @param gridElem
 	 * @return
 	 */
 	private boolean getStretchColumns(JSONObject gridElem)
 	{
 		String stretchColumns = gridElem.optString("stretchColumns");
 		
 		if(stretchColumns != null && stretchColumns.trim().length() > 0)
 		{
 			return Boolean.parseBoolean(stretchColumns);
 		}
 		
 		return false;
 	}
 
 	/**
 	 * @param gridElem
 	 * @return
 	 */
 	private int getCellSpacing(JSONObject gridElem)
 	{
 		String spacing = gridElem.optString("cellSpacing");
 		
 		if(spacing != null && spacing.trim().length() > 0)
 		{
 			return Integer.parseInt(spacing);
 		}
 		
 		return 1;
 	}
 
 	/**
 	 * @author Thiago da Rosa de Bustamante
 	 *
 	 */
 	public static class DataSourceAttributeParser extends AttributeProcessor<WidgetCreatorContext>
 	{
 		public DataSourceAttributeParser(WidgetCreator<?> widgetCreator)
         {
 	        super(widgetCreator);
         }
 
 		public void processAttribute(SourcePrinter out, WidgetCreatorContext context, String propertyValue)
 		{
 			JClassType dataSourceClass = getWidgetCreator().getContext().getTypeOracle().findType(DataSources.getDataSource(propertyValue));
 			JClassType dtoType = ClassUtils.getReturnTypeFromMethodClass(dataSourceClass, "getBoundObject", new JType[]{});
 
 			String className = PagedDataSource.class.getCanonicalName()+"<"+dtoType.getParameterizedQualifiedSourceName()+">";
 			String dataSource = getWidgetCreator().createVariableName("dataSource");
 			out.println(className+" "+dataSource+" = ("+className+") Screen.createDataSource("+EscapeUtils.quote(propertyValue)+");");
 			String widget = context.getWidget();			
 			String dataSourceDefinitions = createDataSourceColumnDefinitions(out, context.getWidgetElement(), dtoType, context.getWidgetId());
 			out.println(dataSource+".setColumnDefinitions("+dataSourceDefinitions+");");
 			out.println(widget+".setDataSource("+dataSource+");");
 		}
 
 		private String createDataSourceColumnDefinitions(SourcePrinter out, JSONObject gridElem, JClassType dtoType, String gridId)
         {
 			String colDefs = getWidgetCreator().createVariableName("colDefs");
 			
 			String dtoClassName = dtoType.getParameterizedQualifiedSourceName();
 			String columnDefinitionsClassName = br.com.sysmap.crux.core.client.datasource.ColumnDefinitions.class.getCanonicalName()+"<"+dtoClassName+">";
 			out.println(columnDefinitionsClassName+" "+colDefs+" = new "+columnDefinitionsClassName+"();");
 
 			JSONArray colElems = ensureChildren(gridElem, false);
 			int colsSize = colElems.length();
 			if(colsSize > 0)
 			{
 				for (int i=0; i<colsSize; i++)
 				{
 					JSONObject colElem = colElems.optJSONObject(i);
 					if (colElem != null)
 					{
 						String columnType = getChildName(colElem);
 						if("dataColumn".equals(columnType))
 						{
 							StringBuilder getValueExpression = new StringBuilder();
 							String colKey = colElem.optString("key");
 							JType propType;
 							try
 							{
								propType = ClassUtils.buildGetValueExpression(getValueExpression, dtoType, colKey, "recordObject", true);
 							}
 							catch (Exception e)
 							{
 						        throw new CruxGeneratorException(widgetMessages.gridErrorInvalidColumn(gridId, colKey));
 							}
 							
 							JClassType comparableType = getWidgetCreator().getContext().getTypeOracle().findType(Comparable.class.getCanonicalName());
 							
 							boolean isSortable = (propType.isPrimitive() != null) || (comparableType.isAssignableFrom((JClassType) propType));
 							String propTypeName = propType.getParameterizedQualifiedSourceName();
 							out.println(colDefs+".addColumn(new "+br.com.sysmap.crux.core.client.datasource.ColumnDefinition.class.getCanonicalName()+
 									    "<"+propTypeName+","+dtoClassName+">("+EscapeUtils.quote(colElem.optString("key"))+","+isSortable+"){");
 							out.println("public "+propTypeName+" getValue("+dtoClassName+" recordObject){");
 							out.println("return "+getValueExpression.toString());
 							out.println("}");
 							out.println("});");
 						}
 					}
 				}
 			}
 			
 			return colDefs;
         }
 	}
 
 	/**
 	 * @param gridElem
 	 * @return
 	 */
 	private String getRowSelectionModel(JSONObject gridElem)
 	{
 		String rowSelection = gridElem.optString("rowSelection");
 		
 		RowSelectionModel ret = RowSelectionModel.unselectable;
 		if(rowSelection != null && rowSelection.length() > 0)
 		{
 			if("unselectable".equals(rowSelection))
 			{
 				ret = RowSelectionModel.unselectable;
 			}
 			else if("single".equals(rowSelection))
 			{
 				ret = RowSelectionModel.single;
 			}
 			else if("multiple".equals(rowSelection))
 			{
 				ret = RowSelectionModel.multiple;
 			}
 			else if("singleRadioButton".equals(rowSelection))
 			{
 				ret = RowSelectionModel.singleRadioButton;
 			}
 			else if("multipleCheckBox".equals(rowSelection))
 			{
 				ret = RowSelectionModel.multipleCheckBox;
 			}
 			else if("multipleCheckBoxSelectAll".equals(rowSelection))
 			{
 				ret = RowSelectionModel.multipleCheckBoxSelectAll;
 			}
 		}
 		
 		return RowSelectionModel.class.getCanonicalName()+"."+ret.toString();
 	}
 
 	/**
 	 * @param gridElem
 	 * @return
 	 */
 	private int getPageSize(JSONObject gridElem)
 	{
 		String pageSize = gridElem.optString("pageSize");
 		
 		if(pageSize != null && pageSize.length() > 0)
 		{
 			return Integer.parseInt(pageSize);
 		}
 		
 		return Integer.MAX_VALUE;
 	}
 
 	/**
 	 * @param out
 	 * @param gridElem
 	 * @return
 	 * @throws CruxGeneratorException
 	 */
 	private String getColumnDefinitions(SourcePrinter out, JSONObject gridElem) throws CruxGeneratorException
 	{
 		String defs = createVariableName("defs");
 		
 		out.println(ColumnDefinitions.class.getCanonicalName()+" "+defs+" = new "+ColumnDefinitions.class.getCanonicalName()+"();");
 
 		JSONArray colElems = ensureChildren(gridElem, false);
 		int colsSize = colElems.length();
 		if(colsSize > 0)
 		{
 			for (int i=0; i<colsSize; i++)
 			{
 				JSONObject colElem = colElems.optJSONObject(i);
 				if (colElem != null)
 				{
 					String width = colElem.optString("width");
 					String strVisible = colElem.optString("visible");
 					String strSortable = colElem.optString("sortable");					
 					String strWrapLine = colElem.optString("wrapLine");
 					String label = colElem.optString("label");
 					String key = colElem.optString("key");
 					String strFormatter = colElem.optString("formatter");
 					String hAlign = colElem.optString("horizontalAlignment");
 					String vAlign = colElem.optString("verticalAlignment");
 
 					boolean visible = (strVisible != null && strVisible.length() > 0) ? Boolean.parseBoolean(strVisible) : true;
 					boolean sortable = (strSortable != null && strSortable.length() > 0) ? Boolean.parseBoolean(strSortable) : true;
 					boolean wrapLine = (strWrapLine != null && strWrapLine.length() > 0) ? Boolean.parseBoolean(strWrapLine) : false;
 					String formatter = (strFormatter != null && strFormatter.length() > 0) ? strFormatter : null;
 					label = (label != null && label.length() > 0) ? getDeclaredMessage(label) : EscapeUtils.quote("");
 
 					String def = createVariableName("def");
 
 					String columnType = getChildName(colElem);
 					if("dataColumn".equals(columnType))
 					{
 						out.println(ColumnDefinition.class.getCanonicalName()+" "+def+" = new "+DataColumnDefinition.class.getCanonicalName()+"("+
 								label+", "+
 								EscapeUtils.quote(width)+", "+
 								Formatters.getFormatterInstantionCommand(formatter)+", "+ 
 								visible+", "+
 								sortable+", "+
 								wrapLine+", "+
 								AlignmentAttributeParser.getHorizontalAlignment(hAlign, HasHorizontalAlignment.class.getCanonicalName()+".ALIGN_CENTER")+", "+
 								AlignmentAttributeParser.getVerticalAlignment(vAlign, HasVerticalAlignment.class.getCanonicalName()+".ALIGN_MIDDLE")+");");
 					}
 					else if("widgetColumn".equals(columnType))
 					{
 						String widgetCreator = getWidgetColumnCreator(out, colElem);
 						
 						out.println(ColumnDefinition.class.getCanonicalName()+" "+def+" = new "+WidgetColumnDefinition.class.getCanonicalName()+"("+
 								label+", "+
 								EscapeUtils.quote(width)+", "+
 								widgetCreator+", "+
 								visible+", "+
 								AlignmentAttributeParser.getHorizontalAlignment(hAlign, HasHorizontalAlignment.class.getCanonicalName()+".ALIGN_CENTER")+", "+
 								AlignmentAttributeParser.getVerticalAlignment(vAlign, HasVerticalAlignment.class.getCanonicalName()+".ALIGN_MIDDLE")+");");
 					}
 					else
 					{
 						throw new CruxGeneratorException(widgetMessages.gridErrorInvalidColumnType(gridElem.optString("id")));
 					}
 
 					out.print(defs+".add("+EscapeUtils.quote(key)+", "+def+");");
 				}
 			}
 		}
 		else
 		{
 			throw new CruxGeneratorException(widgetMessages.gridDoesNotHaveColumns(gridElem.optString("id")));
 		}
 				
 		return defs;
 	}
 	
 	/**
 	 * @param out
 	 * @param colElem
 	 * @return
 	 */
 	private String getWidgetColumnCreator(SourcePrinter out, JSONObject colElem)
     {
 	    String colDef = createVariableName("colDef");
 	    String className = WidgetColumnCreator.class.getCanonicalName();
 	    
 	    out.println(className+" "+colDef+" = new "+className+"(){");
 	    out.println("public Widget createWidgetForColumn(){");
 	    
 	    JSONObject child = ensureFirstChild(colElem, false);
 		String childWidget = createChildWidget(out, child, false);//TODO: o parametro addoToScreen deve ser propagado para os filhos da widget criada
         out.println("return "+childWidget+";");
 	    
 	    out.println("};");
 	    out.println("};");
 
 	    return colDef;
     }
 	
 	@TagConstraints(maxOccurs="unbounded")
 	@TagChildren({
 		@TagChild(DataColumnProcessor.class),
 		@TagChild(WidgetColumnProcessor.class)
 	})
 	public static class ColumnProcessor extends ChoiceChildProcessor<WidgetCreatorContext>
 	{
 		@Override
 		public void processChildren(SourcePrinter out, WidgetCreatorContext context) throws CruxGeneratorException {}
 	}
 
 	
 	@TagConstraints(tagName="dataColumn", minOccurs="0", maxOccurs="unbounded")
 	@TagAttributesDeclaration({
 		@TagAttributeDeclaration("width"),
 		@TagAttributeDeclaration(value="visible", type=Boolean.class),
 		@TagAttributeDeclaration(value="sortable", type=Boolean.class, defaultValue="true"),
 		@TagAttributeDeclaration(value="wrapLine", type=Boolean.class, defaultValue="false"),
 		@TagAttributeDeclaration("label"),
 		@TagAttributeDeclaration(value="key", required=true),
 		@TagAttributeDeclaration("formatter"),
 		@TagAttributeDeclaration(value="horizontalAlignment", type=HorizontalAlignment.class, defaultValue="defaultAlign"),
 		@TagAttributeDeclaration(value="verticalAlignment", type=VerticalAlignment.class)
 	})
 	public static class DataColumnProcessor extends WidgetChildProcessor<WidgetCreatorContext> {}
 
 	@TagConstraints(tagName="widgetColumn", minOccurs="0", maxOccurs="unbounded")
 	@TagAttributesDeclaration({
 		@TagAttributeDeclaration("width"),
 		@TagAttributeDeclaration(value="visible", type=Boolean.class),
 		@TagAttributeDeclaration("label"),
 		@TagAttributeDeclaration(value="key", required=true),
 		@TagAttributeDeclaration(value="horizontalAlignment", type=HorizontalAlignment.class, defaultValue="defaultAlign"),
 		@TagAttributeDeclaration(value="verticalAlignment", type=VerticalAlignment.class)
 	})
 	@TagChildren({
 		@TagChild(WidgetProcessor.class)
 	})
 	public static class WidgetColumnProcessor extends WidgetChildProcessor<WidgetCreatorContext> {}
 	
 	public static class WidgetProcessor extends AnyWidgetChildProcessor<WidgetCreatorContext>{}
 	
 	@Override
     public WidgetCreatorContext instantiateContext()
     {
 	    return new WidgetCreatorContext();
     }
 }
