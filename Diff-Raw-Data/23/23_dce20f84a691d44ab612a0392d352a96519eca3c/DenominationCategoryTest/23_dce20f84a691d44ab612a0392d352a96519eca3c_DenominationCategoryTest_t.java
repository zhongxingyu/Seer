 package com.github.croesch.partimana.model.filter.cat.p;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import com.github.croesch.partimana.i18n.Text;
 import com.github.croesch.partimana.model.api.IFilterType;
 import com.github.croesch.partimana.model.filter.cat.c.LocationCategory;
 import com.github.croesch.partimana.model.filter.types.DenominationEquals;
 import com.github.croesch.partimana.model.filter.types.DenominationNotEquals;
 import com.github.croesch.partimana.model.filter.types.StringEquals;
 import com.github.croesch.partimana.types.CountyCouncil;
 import com.github.croesch.partimana.types.Denomination;
 import com.github.croesch.partimana.types.Gender;
 import com.github.croesch.partimana.types.Participant;
 import java.util.Date;
 import org.junit.Test;
 
 /**
  * Provides test cases for {@link DenominationCategory}.
  *
  * @author croesch
  * @since Date: Oct 27, 2012
  */
 public class DenominationCategoryTest {
 
   private DenominationCategory category;
 
   @org.junit.Before
   public void setUp() {
     this.category = new DenominationCategory();
   }
 
   @Test
   public void testGetFilters() {
     assertThat(this.category.getFilterTypes()).containsOnly(new DenominationEquals(), new DenominationNotEquals());
 
     final IFilterType<Denomination> filterType = this.category.getFilterTypes().get(0);
     filterType.setFilterValue(Denomination.OTHER);
 
     assertThat(this.category.getFilterTypes().contains(filterType)).isFalse();
   }
 
   @Test
   public void testSetAndGetFilter() {
     assertThat(this.category.getFilter()).isNull();
     this.category.setFilter(new DenominationEquals());
     assertThat(this.category.getFilter()).isEqualTo(new DenominationEquals());
     this.category.setFilter(null);
     assertThat(this.category.getFilter()).isNull();
   }
 
   @Test
   public void testGetShortDescription() {
     assertThat(this.category.getShortDescription()).isEqualTo(Text.FILTER_CAT_PARTICIPANT_DENOMINATION.text());
    assertThat(this.category.getShortDescription()).isEqualTo(Text.PARTICIPANT_DENOMINATION.text());
   }
 
   @Test
   public void testIsMatchingFilter() {
     assertThat(this.category.isMatchingFilter(null)).isFalse();
 
     final Participant p = new Participant("Musterfrau",
                                           "Maxi",
                                           Gender.FEMALE,
                                           Denomination.CATHOLIC,
                                           new Date(100000000),
                                           "street",
                                           12,
                                           "city",
                                           CountyCouncil.COUNTY_RHEIN_PFALZ);
     assertThat(this.category.isMatchingFilter(p)).isFalse();
 
     final DenominationEquals equals = new DenominationEquals();
     equals.setFilterValue(Denomination.EVANGELIC);
     this.category.setFilter(equals);
     assertThat(this.category.isMatchingFilter(p)).isFalse();
 
     final DenominationNotEquals notEquals = new DenominationNotEquals();
     notEquals.setFilterValue(Denomination.EVANGELIC);
     this.category.setFilter(notEquals);
     assertThat(this.category.isMatchingFilter(p)).isTrue();
   }
 
   @Test
   public void testEquals() {
     assertThat(this.category).isEqualTo(new DenominationCategory());
     assertThat(this.category).isEqualTo(this.category);
     assertThat(this.category).isNotEqualTo(null);
     assertThat(this.category).isNotEqualTo(new LocationCategory());
     assertThat(this.category).isNotEqualTo("category");
 
     this.category.setFilter(new DenominationEquals());
     final DenominationCategory other = new DenominationCategory();
     assertThat(this.category).isNotEqualTo(other);
     assertThat(other).isNotEqualTo(this.category);
     other.setFilter(new DenominationEquals());
     assertThat(this.category).isEqualTo(other);
 
     final LocationCategory different = new LocationCategory();
     different.setFilter(new StringEquals());
     this.category.getFilter().setFilterValue(Denomination.MUSLIM);
     assertThat(this.category).isNotEqualTo(different);
     assertThat(different).isNotEqualTo(this.category);
   }
 
   @Test
   public void testHashCode() {
     assertThat(this.category.hashCode()).isEqualTo(new DenominationCategory().hashCode());
     assertThat(this.category.hashCode()).isEqualTo(this.category.hashCode());
     assertThat(this.category.hashCode()).isEqualTo(new LocationCategory().hashCode());
 
     this.category.setFilter(new DenominationEquals());
     final DenominationCategory other = new DenominationCategory();
     other.setFilter(new DenominationEquals());
     assertThat(this.category.hashCode()).isEqualTo(other.hashCode());
 
     final LocationCategory different = new LocationCategory();
     different.setFilter(new StringEquals());
     assertThat(this.category.hashCode()).isEqualTo(different.hashCode());
   }
 }
