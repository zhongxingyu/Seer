 /*
  * Copyright 2006 Davy Verstappen.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.jsmiparser;
 
 import org.jsmiparser.smi.SmiVariable;
 import org.jsmiparser.smi.SmiConstants;
 import org.jsmiparser.smi.SmiMib;
 import org.jsmiparser.smi.SmiModule;
 import org.jsmiparser.smi.SmiPrimitiveType;
 import org.jsmiparser.smi.SmiRow;
 import org.jsmiparser.smi.SmiTable;
 import org.jsmiparser.smi.SmiTextualConvention;
 import org.jsmiparser.smi.SmiType;
 import org.jsmiparser.smi.SmiVersion;
 import org.jsmiparser.smi.StatusV2;
 import org.jsmiparser.smi.SmiIndex;
 import org.jsmiparser.smi.SmiOidNode;
 
 import java.net.URISyntaxException;
 import java.util.List;
 
 public class IfMibTest extends AbstractMibTestCase {
 
     public IfMibTest() {
         super(SmiVersion.V2,
                 LIBSMI_MIBS_URL + "/iana/IANAifType-MIB",
                 LIBSMI_MIBS_URL + "/ietf/IF-MIB");
     }
 
     public void testSizes() {
         assertEquals(44, getMib().getScalars().size());
         assertEquals(57, getMib().getColumns().size());
        assertEquals(2, getMib().getNotificationTypes().size());
 
         SmiModule ifMib = getMib().findModule("IF-MIB");
         assertNotNull(ifMib);
 
         assertEquals(3, ifMib.getScalars().size());
         assertEquals(22+19+3+3+6, ifMib.getColumns().size());
 
         assertNotNull(ifMib.findScalar("ifNumber"));
         assertNull(ifMib.findColumn("ifNumber"));
 
         assertNotNull(ifMib.findColumn("ifName"));
         assertNull(ifMib.findScalar("ifName"));
 
         assertNotNull(ifMib.findTable("ifTable"));
         assertNull(ifMib.findVariable("ifTable"));
 
         assertNotNull(ifMib.findRow("ifEntry"));
         assertNull(ifMib.findVariable("ifEntry"));
     }
 
     public void testInterfaceIndex() {
         assertEquals(7, getMib().getModules().size());
 
         SmiTextualConvention interfaceIndex = getMib().getTextualConventions().find("InterfaceIndex");
         assertNotNull(interfaceIndex);
         assertEquals("InterfaceIndex", interfaceIndex.getId());
         assertEquals("IF-MIB", interfaceIndex.getModule().getId());
         String source = interfaceIndex.getModule().getIdToken().getLocation().getSource();
         assertTrue(source.contains("IF-MIB"));
         assertEquals(SmiTextualConvention.class, interfaceIndex.getClass());
         assertNotNull(interfaceIndex.getRangeConstraints());
         assertEquals(1, interfaceIndex.getRangeConstraints().size());
         assertSame(getInteger32(), interfaceIndex.getBaseType());
         assertEquals(SmiPrimitiveType.INTEGER_32, interfaceIndex.getPrimitiveType());
         assertEquals(SmiConstants.INTEGER_TYPE, interfaceIndex.getBaseType().getBaseType());
         assertNull(interfaceIndex.getBaseType().getBaseType().getBaseType());
         assertEquals("d", interfaceIndex.getDisplayHint());
         assertEquals(StatusV2.CURRENT, interfaceIndex.getStatusV2());
         assertTrue(interfaceIndex.getDescription().startsWith("A unique value"));
         assertFalse(interfaceIndex.getDescription().endsWith("\""));
         assertNull(interfaceIndex.getReference());
 
         assertNull(interfaceIndex.getEnumValues());
         assertNull(interfaceIndex.getBitFields());
         assertNull(interfaceIndex.getFields());
         assertNull(interfaceIndex.getSizeConstraints());
     }
 
     public void testIfTable() throws URISyntaxException {
 
         SmiMib mib = getMib();
 
         SmiTable ifTable = mib.getTables().find("ifTable");
         assertNotNull(ifTable);
         assertEquals("1.3.6.1.2.1.2.2", ifTable.getOidStr());
 
         SmiRow ifEntry = mib.getRows().find("ifEntry");
         assertNotNull(ifEntry);
         assertSame(ifTable.getNode(), ifEntry.getNode().getParent());
         assertSame(ifTable, ifEntry.getTable());
         assertSame(ifEntry, ifTable.getRow());
         assertEquals("1.3.6.1.2.1.2.2.1", ifEntry.getOidStr());
         assertSame(ifTable, ifEntry.getTable());
 
         SmiVariable ifIndex = mib.getVariables().find("ifIndex");
         assertNotNull(ifIndex);
         assertSame(ifEntry.getNode(), ifIndex.getNode().getParent());
         assertSame(ifTable.getNode(), ifIndex.getNode().getParent().getParent());
         assertEquals("1.3.6.1.2.1.2.2.1.1", ifIndex.getOidStr());
         SmiTextualConvention interfaceIndex = getMib().getTextualConventions().find("InterfaceIndex");
         assertNotNull(interfaceIndex);
         assertSame(interfaceIndex, ifIndex.getType());
 
         List<SmiIndex> ifIndexes = ifEntry.getIndexes();
         assertEquals(1, ifIndexes.size());
         SmiIndex index = ifIndexes.get(0);
         assertSame(ifIndex, index.getColumn());
         assertSame(ifEntry, index.getRow());
         assertEquals(false, index.isImplied());
 
         SmiVariable ifAdminStatus = mib.getVariables().find("ifAdminStatus");
         assertNotNull(ifAdminStatus);
         assertEquals(ifEntry, ifAdminStatus.getRow());
         assertEquals(ifTable, ifAdminStatus.getTable());
         assertEquals("1.3.6.1.2.1.2.2.1.7", ifAdminStatus.getOidStr());
         assertSame(ifEntry.getNode(), ifAdminStatus.getNode().getParent());
 
         SmiType ifAdminStatusType = ifAdminStatus.getType();
         assertSame(SmiPrimitiveType.ENUM, ifAdminStatusType.getPrimitiveType());
         assertEquals(SmiConstants.INTEGER_TYPE, ifAdminStatusType.getBaseType());
         assertNull(ifAdminStatusType.getBaseType().getBaseType());
         assertEquals(3, ifAdminStatusType.getEnumValues().size());
         assertEquals("up", ifAdminStatusType.getEnumValues().get(0).getId());
         assertEquals(1, ifAdminStatusType.getEnumValues().get(0).getValue().intValue());
         assertEquals("down", ifAdminStatusType.getEnumValues().get(1).getId());
         assertEquals(2, ifAdminStatusType.getEnumValues().get(1).getValue().intValue());
         assertEquals("testing", ifAdminStatusType.getEnumValues().get(2).getId());
         assertEquals(3, ifAdminStatusType.getEnumValues().get(2).getValue().intValue());
 
         // mib.getRootNode().dumpTree(System.out, "");
 
         List<SmiVariable> columns = ifTable.getRow().getColumns();
         assertEquals(22, columns.size());
         assertTrue(columns.contains(ifIndex));
         assertTrue(columns.contains(ifAdminStatus));
 
         SmiModule ifMib = mib.findModule("IF-MIB");
         assertNotNull(ifMib);
         System.out.println(mib.getVariables().size() + mib.getTables().size() + mib.getRows().size());
 
         checkOidTree(mib);
     }
 
     public void testFindByOidPrefix() {
         SmiMib mib = getMib();
 
         SmiVariable ifAdminStatus = mib.getVariables().find("ifAdminStatus");
         assertNotNull(ifAdminStatus);
         assertEquals("1.3.6.1.2.1.2.2.1.7", ifAdminStatus.getOidStr());
 
         int[] adminStatusOfInterface0x1101 = new int[]{1, 3, 6, 1, 2, 1, 2, 2, 1, 7, 0x1101};
         SmiOidNode result = mib.findByOidPrefix(adminStatusOfInterface0x1101);
         assertSame(ifAdminStatus.getNode(), result);
         assertEquals(adminStatusOfInterface0x1101.length, result.getOid().length + 1);
     }
 
     public void testFindByOid() {
         SmiMib mib = getMib();
 
         SmiVariable ifAdminStatus = mib.getVariables().find("ifAdminStatus");
         assertNotNull(ifAdminStatus);
         assertEquals("1.3.6.1.2.1.2.2.1.7", ifAdminStatus.getOidStr());
 
         SmiOidNode result = mib.findByOid(1, 3, 6, 1, 2, 1, 2, 2, 1, 7);
         assertNotNull(result);
         assertSame(ifAdminStatus.getNode(), result);
 
         result = mib.findByOid(1, 3);
         assertNotNull(result);
         assertEquals(1, result.getValues().size());
         assertEquals("org", result.getValues().get(0).getId());
     }
 
 }
