 package fr.xebia.katas.gildedrose;
 
 import java.util.List;
 
 public class Inn {
 	public static final String NORMAL_ITEM_NAME = "+5 Dexterity Vest";
 	public static final String BRIE_ITEM_NAME = "Aged Brie";
 	public static final String NORMAL_MONGOOSE_ITEM_NAME = "Elixir of the Mongoose";
 	public static final String LEGENDARY_ITEM_NAME = "Sulfuras, Hand of Ragnaros";
 	public static final String BACKSTAGE_ITEM_NAME = "Backstage passes to a TAFKAL80ETC concert";
 	public static final String CONJURED_ITEM_NAME = "Conjured Mana Cake";
 
 	private List<Item> items;
 
 	public Inn( List<Item> items ) {
 		this.items = items;
 	}
 
 	public List<Item> getItems() {
 		return items;
 	}
 
 	public void updateQuality() {
 		for( int i = 0; i < items.size(); i++ ) {
 
 			boolean isNormal =
 					!items.get( i ).getName().equals( "Aged Brie" ) && !items.get( i ).getName().equals( "Backstage passes to a TAFKAL80ETC concert" );
 
 			if( isNormal ) {
 				decrementItemQualityForNormalItem( i );
 			} else {
 				increaseQualityForBrieOrBackstageItem( i );
 			}
 
 			decrementSellinForItem( i );
 
 			if( items.get( i ).getSellIn() < 0 ) {
 				if( !items.get( i ).getName().equals( "Aged Brie" ) ) {
 					if( !items.get( i ).getName().equals( "Backstage passes to a TAFKAL80ETC concert" ) ) {
 				    	decrementItemQualityForNormalItem( i );
 					} else {
 						items.get( i ).setQuality( 0 );
 					}
 				} else {
 					incrementItemQuality( i );
 				}
 			}
 		}
 	}
 
 	private void decrementSellinForItem( int i ) {
 		if( !items.get( i ).getName().equals( "Sulfuras, Hand of Ragnaros" ) ) {
 			items.get( i ).setSellIn( items.get( i ).getSellIn() - 1 );
 		}
 	}
 
 	private void decrementItemQualityForNormalItem( int i ) {
 		if( items.get( i ).getQuality() > 0 ) {
 			if( !items.get( i ).getName().equals( "Sulfuras, Hand of Ragnaros" ) ) {
 				items.get( i ).setQuality( items.get( i ).getQuality() - 1 );
 			}
 
			/*if( items.get( i ).getName().equals( CONJURED_ITEM_NAME ) ) {
 				decrementItemQualityForNormalItem( i );
			}*/
 		}
 	}
 
 	private void increaseQualityForBrieOrBackstageItem( int i ) {
 		incrementItemQuality( i );
 
 		if( isMinusThan50( i ) ) {
 			if( items.get( i ).getName().equals( "Backstage passes to a TAFKAL80ETC concert" ) ) {
 				if( items.get( i ).getSellIn() < 11 ) {
 					incrementItemQuality( i );
 				}
 
 				if( items.get( i ).getSellIn() < 6 ) {
 					incrementItemQuality( i );
 				}
 			}
 		}
 	}
 
 	private boolean isMinusThan50( int i ) {
 		return items.get( i ).getQuality() < 50;
 	}
 
 	private void incrementItemQuality( int i ) {
 		if( isMinusThan50( i ) ) {
 			items.get( i ).setQuality( items.get( i ).getQuality() + 1 );
 		}
 	}
 }
