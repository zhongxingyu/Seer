 package fr.xebia.katas.gildedrose;
 
 import com.google.common.base.Function;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import static com.google.common.collect.Collections2.transform;
 import static fr.xebia.katas.gildedrose.Inn.*;
 import static org.fest.assertions.Assertions.assertThat;
 
 public class InnTest {
 
 	final int MAX_PERMITTED_QUALITY_VALUE = 50;
 	final int MIN_PERMITTED_QUALITY_VALUE = 0;
 	
 	final String QUALITY_PROPERTY_NAME = "quality";
 
 	final int DECREASE_NORMAL_ITEM_AFTER_ONE_DAY = -1;
 	final int INCREASE_AGED_ITEM_AFTER_ONE_DAY = 1;
 	final int NOT_DECREASE_LEGEND_ITEM_AFTER_ONE_DAY = 0;
 	final int INCREASE_BACKSTAGE_ITEM_AFTER_ONE_DAY = 1;
	final int DECREASE_CONJURED_ITEM_AFTER_ONE_DAY = -2;
 
 	@Test
 	public void withPreExistantItems_shouldUpdateQuality_afterOneDay() {
 		Inn inn = new Inn( buildPreExistantItems() );
 
 		List<Item> items = inn.getItems();
 
 		List<Integer> previousQualities = cloneItemList( items );
 		inn.updateQuality();
 
 		assertThat( inn.getItems() ).hasSize( 6 );
 
 		assertThat( inn.getItems() ).onProperty( QUALITY_PROPERTY_NAME ).containsExactly(
 				previousQualities.get( 0 ) + DECREASE_NORMAL_ITEM_AFTER_ONE_DAY,
 				previousQualities.get( 1 ) + INCREASE_AGED_ITEM_AFTER_ONE_DAY,
 				previousQualities.get( 2 ) + DECREASE_NORMAL_ITEM_AFTER_ONE_DAY,
 				previousQualities.get( 3 ) + NOT_DECREASE_LEGEND_ITEM_AFTER_ONE_DAY,
 				previousQualities.get( 4 ) + INCREASE_BACKSTAGE_ITEM_AFTER_ONE_DAY,
				0
				//qualities.get( 5 ) + DECREASE_CONJURED_ITEM_AFTER_ONE_DAY
 		);
 	}
 
 	@Test
 	public void qualityShouldBeZero_whenSellIn_overflow_forNormalItem() {
 		Inn inn = new Inn( Arrays.asList( new Item( NORMAL_ITEM_NAME, -1, 2 ) ) );
 		inn.updateQuality();
 
 		assertThat( returnFirstItem( inn ).getQuality() ).isEqualTo( MIN_PERMITTED_QUALITY_VALUE );
 	}
 
 	@Test
 	public void qualityShouldStayZero_whenSellIn_overflow_forNormalItem_withZeroQuality() {
 		Inn inn = new Inn( Arrays.asList( new Item( NORMAL_ITEM_NAME, -1, 0 ) ) );
 		inn.updateQuality();
 
 		assertThat( returnFirstItem( inn ).getQuality() ).isEqualTo( MIN_PERMITTED_QUALITY_VALUE );
 	}
 
 	@Test
 	public void qualityShouldNotExceed50_withAgeBrieItem() {
 		Inn inn = new Inn( Arrays.asList( new Item( BRIE_ITEM_NAME, 0, MAX_PERMITTED_QUALITY_VALUE ) ) );
 		inn.updateQuality();
 
 		assertThat( returnFirstItem( inn ).getQuality() ).isEqualTo( MAX_PERMITTED_QUALITY_VALUE );
 	}
 
 	@Test
 	public void qualityShouldBeZero_forBackstageItemsAfterConcert() {
 		Inn inn = new Inn( Arrays.asList( new Item( BACKSTAGE_ITEM_NAME, 0, 1 ) ) );
 		inn.updateQuality();
 
 		assertThat( returnFirstItem( inn ).getQuality() ).isEqualTo( MIN_PERMITTED_QUALITY_VALUE );
 	}
 
 	@Test
 	public void qualityShouldIncreaseBy2_forBackstageItemsAt10DaysToSellin() {
 		Inn inn = new Inn( Arrays.asList( new Item( BACKSTAGE_ITEM_NAME, 9, 1 ) ) );
 		inn.updateQuality();
 
 		assertThat( returnFirstItem( inn ).getQuality() ).isEqualTo( 3 );
 	}
 
 	@Test
 	public void qualityShouldIncreaseBy3_forBackstageItemsAt5DaysToSellin() {
 		Inn inn = new Inn( Arrays.asList( new Item( BACKSTAGE_ITEM_NAME, 3, 1 ) ) );
 		inn.updateQuality();
 
 		assertThat( returnFirstItem( inn ).getQuality() ).isEqualTo( 4 );
 	}
 
 	@Test
 	public void sellInShouldDecrease_afterOneDay() {
 		Inn inn = new Inn( Arrays.asList( new Item( BACKSTAGE_ITEM_NAME, 1, MIN_PERMITTED_QUALITY_VALUE ) ) );
 		inn.updateQuality();
 
 		assertThat( returnFirstItem( inn ).getSellIn() ).isEqualTo( MIN_PERMITTED_QUALITY_VALUE );
 	}
 
 	@Ignore
 	public void qualityShouldDecreaseTwiceFasterThanNormalItem_forConjuredItem() {
 		Inn inn = new Inn( Arrays.asList( new Item( CONJURED_ITEM_NAME, 1, 3 ) ) );
 		inn.updateQuality();
 
 		assertThat( returnFirstItem( inn ).getQuality() ).isEqualTo( 1 );
 	}
 
 	private List<Item> buildPreExistantItems() {
 		List<Item> items = new ArrayList<Item>();
 		items.add( new Item( NORMAL_ITEM_NAME, 10, 20 ) );
 		items.add( new Item( BRIE_ITEM_NAME, 2, MIN_PERMITTED_QUALITY_VALUE ) );
 		items.add( new Item( NORMAL_MONGOOSE_ITEM_NAME, 5, 7 ) );
 		items.add( new Item( LEGENDARY_ITEM_NAME, 0, 80 ) );
 		items.add( new Item( BACKSTAGE_ITEM_NAME, 15, 20 ) );
 		items.add( new Item( CONJURED_ITEM_NAME, 3, 6 ) );
 
 		return items;
 	}
 
 	private ArrayList<Integer> cloneItemList( List<Item> items ) {
 		return new ArrayList<Integer>( transform( items, new Function<Item, Integer>() {
 			public Integer apply( Item item ) {
 				return item.getQuality();
 			}
 		} ) );
 	}
 
 	private Item returnFirstItem( Inn inn ) {
 		return inn.getItems().get( 0 );
 	}
 }
