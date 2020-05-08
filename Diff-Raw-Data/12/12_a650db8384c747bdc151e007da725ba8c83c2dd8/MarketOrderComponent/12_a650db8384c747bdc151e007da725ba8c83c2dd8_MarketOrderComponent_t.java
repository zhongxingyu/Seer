 /**
  * Copyright 2004-2009 Tobias Gierke <tobias.gierke@code-sourcery.de>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.codesourcery.eve.skills.ui.components.impl;
 
 import java.awt.GridBagLayout;
 import java.util.ArrayList;
import java.util.Comparator;
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 
 import de.codesourcery.eve.apiclient.IAPIClient;
 import de.codesourcery.eve.apiclient.datamodel.APIResponse;
 import de.codesourcery.eve.apiclient.datamodel.RequestOptions;
 import de.codesourcery.eve.skills.accountdata.IUserAccountStore;
 import de.codesourcery.eve.skills.datamodel.ICharacter;
 import de.codesourcery.eve.skills.datamodel.MarketOrder;
 import de.codesourcery.eve.skills.ui.components.AbstractComponent;
 import de.codesourcery.eve.skills.ui.components.ISelectionListener;
 import de.codesourcery.eve.skills.ui.components.ISelectionProvider;
 import de.codesourcery.eve.skills.ui.model.AbstractTableModel;
 import de.codesourcery.eve.skills.ui.model.AbstractViewFilter;
 import de.codesourcery.eve.skills.ui.model.IViewFilter;
 import de.codesourcery.eve.skills.ui.model.TableColumnBuilder;
 import de.codesourcery.eve.skills.ui.utils.UITask;
 import de.codesourcery.eve.skills.util.AmountHelper;
 import de.codesourcery.eve.skills.utils.DateHelper;
 
 public class MarketOrderComponent extends AbstractComponent
 {
 
 	private final JTable table = new JTable();
 	private final MyTableModel model = new MyTableModel();
 	
 	@Resource(name="api-client")
 	private IAPIClient apiClient;
 	
 	@Resource(name="useraccount-store")
 	private IUserAccountStore userAccountStore;
 	
 	private final ISelectionProvider<ICharacter> charProvider;
 	
 	private final IViewFilter<MarketOrder> viewFilter = new AbstractViewFilter<MarketOrder>() {
 
 		@Override
 		public boolean isHiddenUnfiltered(MarketOrder value)
 		{
 			return ! value.hasState( 
 					MarketOrder.OrderState.OPEN ,  
 					MarketOrder.OrderState.PENDING );
 		}
 		
 	};
 
 	
 	private final ISelectionListener<ICharacter> listener =
 		new ISelectionListener<ICharacter>() {
 
 		@Override
 		public void selectionChanged(ICharacter selected)
 		{
 			refresh();
 		}
 	}; 
 	
 	public MarketOrderComponent(ISelectionProvider<ICharacter> currentCharacter) {
 		if ( currentCharacter == null ) {
 			throw new IllegalArgumentException(
 					"currentCharacter cannot be NULL");
 		}
 		this.charProvider = currentCharacter;
 		this.charProvider.addSelectionListener( listener ); 
 	}
 	
 	@Override
 	protected JPanel createPanel()
 	{
 		
 		table.setModel( model );
 		table.setRowSorter( model.getRowSorter() );
 		model.setViewFilter( viewFilter );
 		
 		final JPanel result = new JPanel();
 		result.setLayout( new GridBagLayout() );
 		result.add( new JScrollPane( table ) , constraints().resizeBoth().useRemainingSpace().end() );
 		
 		return result;
 	}
 	
 	@Override
 	protected void onAttachHook(IComponentCallback callback)
 	{
 		refresh();
 	}
 	
 	@Override
 	protected void onDetachHook()
 	{
 		charProvider.removeSelectionListener( listener );
 	}
 	
 	@Override
 	protected void disposeHook()
 	{
 		charProvider.removeSelectionListener( listener );
 	}
 
 	protected void refresh() {
 		
 		final ICharacter character = this.charProvider.getSelectedItem();
 		
 		if ( character == null ) {
 			refresh( new ArrayList<MarketOrder>() );
 			return;
 		}
 		
 		submitTask( new UITask() {
 
 			private List<MarketOrder> marketOrders;
 			
 			@Override
 			public String getId()
 			{
 				return "fetch_market_orders_for_"+character.getCharacterId().getValue();
 			}
 
 			@Override
 			public void run() throws Exception
 			{
 
 				displayStatus("Fetching market orders for "+character.getName());
 				final APIResponse<List<MarketOrder>> response = apiClient.getMarketOrders( 
 						character ,
 						userAccountStore.getAccountByCharacterID( character.getCharacterId() ),
 						RequestOptions.DEFAULT );
 				
 				this.marketOrders = response.getPayload();
 			}
 			
 			@Override
 			public void failureHook(Throwable t) throws Exception
 			{
 				displayError("Failed to retrieve market orders" ,t );
 				refresh( new ArrayList<MarketOrder>() );
 			}
 
 			@Override
 			public void successHook() throws Exception
 			{
 				refresh( marketOrders );
 			}
 		});
 	}
 	
 	protected void refresh(final List<MarketOrder> orders) {
 		
 		runOnEventThread( new Runnable() {
 
 			@Override
 			public void run()
 			{
 				model.setData( orders );
 			}} );
 	}
 
 	private final class MyTableModel extends AbstractTableModel<MarketOrder> {
 
 		private List<MarketOrder> data = 
 			new ArrayList<MarketOrder>();
 		
 		/*
 	private long orderID;
 	private CharacterID characterID;
 	private Station station;
 	private long volumeEntered;
 	private long volumeRemaining;
 	private long minVolume;
 	private OrderState state;
 	private InventoryType itemType;
 	private int range;
 	private int accountKey;
 	private int durationInDays;
 	private long moneyInEscrow;
 	private long price;
 	private PriceInfo.Type type;
 	private EveDate issueDate;		 
 		 */
 		public MyTableModel() {
 			super( 
 				new TableColumnBuilder()
 				.add("Issue date")
 				.add("Item")
 				.add("Type")
 				.add("State")
				.add("Price",String.class, new Comparator<String>() {

					@Override
					public int compare(String o1, String o2) {
						long va1 = AmountHelper.parseISKAmount( o1 );
						long va2 = AmountHelper.parseISKAmount( o2 );
						return Long.compare(va1,va2);
					}
				})
 				.add("Station")
 				.add("Volume",Long.class)
 				.add("Volume remaining",Long.class)
 				.add("Escrow")
 				.add("Duration",Integer.class)
 			);
 		}
 		
 		public void setData(List<MarketOrder> data) {
 			if ( data == null ) {
 				throw new IllegalArgumentException("data cannot be NULL");
 			}
 			this.data = data;
 			modelDataChanged();
 		}
 		
 		@Override
 		protected Object getColumnValueAt(int modelRowIndex,
 				int modelColumnIndex)
 		{
 			final MarketOrder order = 
 				getRow(modelRowIndex);
 			
 			switch( modelColumnIndex ) {
 				case 0:
 					return DateHelper.format( order.getIssueDate().getLocalTime() );
 				case 1:
 					return order.getItemType().getName();
 				case 2:
 					return order.isBuyOrder() ? "BUY" : "SELL";
 				case 3:
 					return order.getState().getDisplayName();
 				case 4:
 					return AmountHelper.formatISKAmount( order.getPrice() );
 				case 5:
 					return order.getStation().getDisplayName();
 				case 6:
 					return order.getVolumeEntered();
 				case 7:
 					return order.getVolumeRemaining();
 				case 8:
 					return AmountHelper.formatISKAmount( order.getMoneyInEscrow() );
 				case 9:
 					return order.getDurationInDays();
 					default:
 						throw new IllegalArgumentException("Invalid column "+modelColumnIndex);
 			}
 		}
 
 		@Override
 		public MarketOrder getRow(int modelRow)
 		{
 			if ( modelRow < 0 || modelRow >= data.size() ) {
 				throw new IllegalArgumentException("Invalid model row "+modelRow);
 			}
 			
 			final MarketOrder result =
 				data.get( modelRow );
 			
 			return result;
 		}
 
 		@Override
 		public int getRowCount()
 		{
 			return data.size();
 		}
 		
 	}
 }
