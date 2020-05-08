 package org.motechproject.carereporting.utils.copier;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.motechproject.carereporting.domain.AreaEntity;
 import org.motechproject.carereporting.domain.DwQueryEntity;
 import org.motechproject.carereporting.domain.FrequencyEntity;
 import org.motechproject.carereporting.domain.IndicatorEntity;
 import org.motechproject.carereporting.domain.UserEntity;
 
 import java.math.BigDecimal;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertNull;
 
 public class AbstractEntityCopierTest {
 
     private static final Boolean ADDITIVE = Boolean.FALSE;
     private static final Boolean COMPUTING = Boolean.TRUE;
     private static final String NAME = "name";
     private static final Integer ID = 12;
     private static final AreaEntity AREA_ENTITY = new AreaEntity();
     private static final FrequencyEntity FREQUENCY_ENTITY = new FrequencyEntity();
     private static final BigDecimal TREND = BigDecimal.TEN;
     private static final UserEntity USER = new UserEntity();
     private static final DwQueryEntity DENOMINATOR = new DwQueryEntity();
     private static final DwQueryEntity NUMERATOR = new DwQueryEntity();
 
     private IndicatorEntity indicatorEntity = new IndicatorEntity();
 
     @Before
     public void setup() {
         indicatorEntity.setAdditive(ADDITIVE);
         indicatorEntity.setComputing(COMPUTING);
         indicatorEntity.setName(NAME);
         indicatorEntity.setId(ID);
         indicatorEntity.setArea(AREA_ENTITY);
         indicatorEntity.setDenominator(DENOMINATOR);
         indicatorEntity.setNumerator(NUMERATOR);
         indicatorEntity.setDefaultFrequency(FREQUENCY_ENTITY);
         indicatorEntity.setOwner(USER);
         indicatorEntity.setTrend(TREND);
     }
 
     @Test
     public void testDeepCopy() {
         IndicatorEntity copied = (IndicatorEntity) AbstractEntityCopier.deepCopy(indicatorEntity);
 
         assertNotSame(copied, indicatorEntity);
         assertNotSame(copied.getOwner(), indicatorEntity.getOwner());
         assertNotSame(copied.getNumerator(), indicatorEntity.getNumerator());
         assertNotSame(copied.getDenominator(), indicatorEntity.getDenominator());
         assertNotSame(copied.getDefaultFrequency(), indicatorEntity.getDefaultFrequency());
         assertNotSame(copied.getArea(), indicatorEntity.getArea());
         assertNotSame(copied.getTrend(), indicatorEntity.getTrend());
         assertNotSame(copied.getName(), indicatorEntity.getName());
         assertNotSame(copied.getComputing(), indicatorEntity.getComputing());
         assertNotSame(copied.getAdditive(), indicatorEntity.getAdditive());
         assertEquals(copied.getTrend(), indicatorEntity.getTrend());
         assertEquals(copied.getName(), indicatorEntity.getName());
         assertEquals(copied.getComputing(), indicatorEntity.getComputing());
         assertEquals(copied.getAdditive(), indicatorEntity.getAdditive());
         assertNull(copied.getId());
     }
 
 }
