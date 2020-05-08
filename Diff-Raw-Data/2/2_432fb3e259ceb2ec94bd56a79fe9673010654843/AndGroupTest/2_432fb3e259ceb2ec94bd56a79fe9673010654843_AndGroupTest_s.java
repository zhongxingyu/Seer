 package com.github.croesch.partimana.model.filter;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.github.croesch.partimana.i18n.Text;
 import com.github.croesch.partimana.model.filter.cat.c.LocationCategory;
 import com.github.croesch.partimana.model.filter.cat.c.NameCategory;
 import com.github.croesch.partimana.model.filter.cat.p.ForeNameCategory;
 import com.github.croesch.partimana.model.filter.types.Contains;
 import com.github.croesch.partimana.model.filter.types.NotEquals;
 import com.github.croesch.partimana.types.Camp;
 import com.github.croesch.partimana.types.Participant;
 
 /**
 * Provides test cases for {@link AndGroupTest}.
  * 
  * @author croesch
  * @since Date: Oct 29, 2012
  */
 public class AndGroupTest {
 
   private Camp c1;
 
   private Camp c2;
 
   private Camp c3;
 
   private Camp c4;
 
   private Camp c5;
 
   @Before
   public void setUp() {
     this.c1 = new Camp("OFZ", new Date(15000000), new Date(110000000), "Berlin", "20 USD");
     this.c2 = new Camp("HFZ", new Date(25000000), new Date(210000000), "Frankfurt", "2 EUR");
     this.c3 = new Camp("Freizeit", new Date(35000000), new Date(310000000), "Stuttgart", "2");
     this.c4 = new Camp("Lager", new Date(45000000), new Date(410000000), "Hannover", "10");
     this.c5 = new Camp("Camp", new Date(55000000), new Date(510000000), "München", "200");
   }
 
   @Test
   public void testSetGetFilter() {
     final AndGroup<Participant> group = new AndGroup<Participant>();
     assertThat(group.getFilters()).isEmpty();
 
     final ParticipantFilter filter1 = new ParticipantFilter();
     final ParticipantFilter filter2 = new ParticipantFilter();
     group.add(filter1);
     assertThat(group.getFilters()).containsExactly(new ParticipantFilter());
 
     group.add(filter1);
     assertThat(group.getFilters()).containsOnly(filter1, filter1);
     group.add(filter2);
     assertThat(group.getFilters()).containsOnly(filter1, filter1, filter2);
 
     filter1.setCategory(new ForeNameCategory());
     assertThat(group.getFilters()).containsOnly(filter1, filter1, filter2);
   }
 
   @Test
   public void testFilterWithoutCategory() {
     final AndGroup<Camp> group = new AndGroup<Camp>();
     final List<Camp> campList = Arrays.asList(this.c1, this.c2, this.c3, this.c4, this.c5);
     assertThat(group.filter(campList)).containsExactly(this.c1, this.c2, this.c3, this.c4, this.c5);
   }
 
   @Test
   public void testFilter() {
     final AndGroup<Camp> group = new AndGroup<Camp>();
     final List<Camp> campList = Arrays.asList(this.c1, this.c2, this.c3, this.c4, this.c5);
     final CampFilter campFilter = new CampFilter();
     group.add(campFilter);
     assertThat(group.filter(campList)).isEmpty();
 
     final LocationCategory category = new LocationCategory();
     campFilter.setCategory(category);
     assertThat(group.filter(campList)).isEmpty();
 
     final Contains contains = new Contains();
     category.setFilter(contains);
     assertThat(group.filter(campList)).isEmpty();
 
     contains.setFilterValue("e");
     assertThat(group.filter(campList)).containsExactly(this.c1, this.c4, this.c5);
 
     final CampFilter campFilter2 = new CampFilter();
     group.add(campFilter2);
     assertThat(group.filter(campList)).isEmpty();
 
     final NameCategory category2 = new NameCategory();
     campFilter2.setCategory(category2);
     assertThat(group.filter(campList)).isEmpty();
 
     final NotEquals<String> notEquals = new NotEquals<String>();
     category2.setFilter(notEquals);
     assertThat(group.filter(campList)).isEmpty();
 
     notEquals.setFilterValue("Lager");
     assertThat(group.filter(campList)).containsExactly(this.c1, this.c5);
 
     notEquals.setFilterValue("oFZ");
     assertThat(group.filter(campList)).containsExactly(this.c1, this.c4, this.c5);
 
     notEquals.setFilterValue("OFZ");
     assertThat(group.filter(campList)).containsExactly(this.c4, this.c5);
 
     final CampFilter campFilter3 = new CampFilter();
     group.add(campFilter3);
     assertThat(group.filter(campList)).isEmpty();
 
     final LocationCategory category3 = new LocationCategory();
     campFilter3.setCategory(category3);
     assertThat(group.filter(campList)).isEmpty();
 
     final Contains contains2 = new Contains();
     category3.setFilter(contains2);
     assertThat(group.filter(campList)).isEmpty();
 
     contains2.setFilterValue("r");
     assertThat(group.filter(campList)).containsExactly(this.c4);
 
     final CampFilter campFilter4 = new CampFilter();
     group.add(campFilter4);
     final LocationCategory category4 = new LocationCategory();
     campFilter4.setCategory(category4);
     final Contains contains3 = new Contains();
     category4.setFilter(contains3);
     contains3.setFilterValue("ü");
     assertThat(group.filter(campList)).isEmpty();
 
     final AndGroup<Camp> group3 = new AndGroup<Camp>();
     group3.add(campFilter3);
     final AndGroup<Camp> group1 = new AndGroup<Camp>();
     group1.add(campFilter);
     final AndGroup<Camp> group2 = new AndGroup<Camp>();
     group2.add(campFilter2);
     final AndGroup<Camp> group12 = new AndGroup<Camp>();
     group12.add(campFilter);
     group12.add(group2);
 
     final AndGroup<Camp> groups = new AndGroup<Camp>();
     groups.add(group12);
     groups.add(group3);
 
     assertThat(groups.filter(campList)).containsExactly(this.c4);
     assertThat(group1.filter(campList)).containsExactly(this.c1, this.c4, this.c5);
     assertThat(group2.filter(campList)).containsExactly(this.c2, this.c3, this.c4, this.c5);
     assertThat(group3.filter(campList)).containsExactly(this.c1, this.c2, this.c3, this.c4);
     assertThat(group12.filter(campList)).containsExactly(this.c4, this.c5);
   }
 
   @Test
   public void testShortDescription() {
     assertThat(new AndGroup<Camp>().getShortDescription()).isEqualTo(Text.FILTER_AND.text());
   }
 }
