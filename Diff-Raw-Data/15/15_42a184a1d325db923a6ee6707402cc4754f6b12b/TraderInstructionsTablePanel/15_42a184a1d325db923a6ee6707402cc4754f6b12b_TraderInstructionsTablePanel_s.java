 package com.myrontuttle.fin.trade.web.panels;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.Model;
 
 import com.myrontuttle.fin.trade.adapt.TradeInstruction;
 import com.myrontuttle.fin.trade.web.data.SortableInstructionDataProvider;
 
 public class TraderInstructionsTablePanel extends Panel {
 
 	private static final long serialVersionUID = 1L;
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public TraderInstructionsTablePanel(String id, String traderId) {
 		super(id);
 
 		List<IColumn<TradeInstruction, String>> columns = new ArrayList<IColumn<TradeInstruction, String>>();
 
 		columns.add(new PropertyColumn<TradeInstruction, String>(new Model<String>("ID"), "tradeInstructionId", 
 						"tradeInstructionId"));
		columns.add(new PropertyColumn(new Model<String>("Symbol"), "symbol"));
		columns.add(new PropertyColumn(new Model<String>("Parameters"), "parameters"));
 
 		DataTable dataTable = new DefaultDataTable<TradeInstruction, String>("traderInstructions", columns,
 				new SortableInstructionDataProvider(traderId), 20);
 
 		add(dataTable);
 	}
 
 }
