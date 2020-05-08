 /**************************************************************************************************
  * This file is part of [SpringAtom] Copyright [kornicameister@gmail.com][2013]                   *
  *                                                                                                *
  * [SpringAtom] is free software: you can redistribute it and/or modify                           *
  * it under the terms of the GNU General Public License as published by                           *
  * the Free Software Foundation, either version 3 of the License, or                              *
  * (at your option) any later version.                                                            *
  *                                                                                                *
  * [SpringAtom] is distributed in the hope that it will be useful,                                *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of                                 *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                  *
  * GNU General Public License for more details.                                                   *
  *                                                                                                *
  * You should have received a copy of the GNU General Public License                              *
  * along with [SpringAtom].  If not, see <http://www.gnu.org/licenses/gpl.html>.                  *
  **************************************************************************************************/
 
 package org.agatom.springatom;
 
 import org.agatom.springatom.model.beans.car.SCar;
 import org.agatom.springatom.mvc.model.service.SCarMasterService;
 import org.agatom.springatom.mvc.model.service.SCarService;
 import org.junit.Before;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author kornicameister
  * @version 0.0.1
  * @since 0.0.1
  */
abstract public class AbstractCarTestCase
        extends AbstractTestCaseWithClient {
 
     protected static final List<MockCar>   MOCK_CARS              = new ArrayList<>();
     protected static       Long            MOCKED_MASTER_DISTINCT = null;
     protected static       Map<Long, SCar> MOCKED_CARS            = null;
    protected static       String[]        VINS                   = new String[]{
            "1GTFC24C5CF678861",
            "1GT5C0BG4AZ041882",
            "WDBRF61J11F925139",
            "1FTPX12544N854472",
            "2S3TC52CXX6784490"

    };
     protected static List<MockMaster>  MOCKED_MASTERS;
     @Autowired
     protected        SCarService       carService;
     @Autowired
     protected        SCarMasterService carMasterService;
 
     @Before
     public void setUp() throws Exception {
         super.setUp();
         MOCKED_MASTERS = new ArrayList<>();
 
         MOCKED_MASTERS.add(new MockMaster("Fiat", "Panda"));
         MOCKED_MASTERS.add(new MockMaster("Fiat", "126p"));
         MOCKED_MASTERS.add(new MockMaster("Jeep", "Commander"));
         MOCKED_MASTERS.add(new MockMaster("Fiat", "Panda"));
         MOCKED_MASTERS.add(new MockMaster("Jeep", "Commander"));
 
         MOCKED_MASTER_DISTINCT = 3l;
 
         int it = 0;
         for (final MockMaster mockMaster : MOCKED_MASTERS) {
            final String licencePlate = String.format("E%d KRZ", it);
            final String vinNumber = String.valueOf(VINS[it++]);
             final Long ownerId = C_1.getId();
             MOCK_CARS.add(new MockCar(licencePlate, vinNumber, ownerId, mockMaster));
         }
     }
 
     protected static class MockMaster {
         public final String brand;
         public final String model;
 
         public MockMaster(final String brand, final String model) {
             this.brand = brand;
             this.model = model;
         }
     }
 
     protected static class MockCar {
         public final String     licencePlate;
         public final String     vinNumber;
         public final Long       ownerId;
         public final MockMaster mockMaster;
 
         public MockCar(final String licencePlate, final String vinNumber, final Long ownerId, final MockMaster mockMaster) {
             this.licencePlate = licencePlate;
             this.vinNumber = vinNumber;
             this.ownerId = ownerId;
             this.mockMaster = mockMaster;
         }
     }
 }
