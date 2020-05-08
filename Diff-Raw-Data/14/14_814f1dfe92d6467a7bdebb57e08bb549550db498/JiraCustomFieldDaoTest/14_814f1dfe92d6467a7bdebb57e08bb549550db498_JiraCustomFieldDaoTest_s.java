 /*******************************************************************************
  * Copyright 2012 Brendan Gordon
  * 	
  * 	This file is part of Jira2Db.
  * 	
  * 	Jira2Db is free software: you can redistribute it and/or modify
  * 	it under the terms of the GNU General Public License as published by
  * 	the Free Software Foundation, either version 3 of the License, or
  * 	(at your option) any later version.
  * 	
  * 	Jira2Db is distributed in the hope that it will be useful,
  * 	but WITHOUT ANY WARRANTY; without even the implied warranty of
  * 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * 	GNU General Public License for more details.
  * 	
  * 	You should have received a copy of the GNU General Public License
  * 	along with Jira2Db.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package com.gordcorp.jira2db.persistence;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.gordcorp.jira2db.persistence.dto.JiraCustomFieldDto;
 
 public class JiraCustomFieldDaoTest {
 	static JiraCustomFieldDao jiraCustomFieldDao = null;
 
 	static {
 		try {
 			jiraCustomFieldDao = new JiraCustomFieldDao(
 					JiraCustomFieldDto.class,
 					SqlSessionFactorySingleton.instance());
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static JiraCustomFieldDto getTestJiraCustomFieldDto() {
 		JiraCustomFieldDto dto = new JiraCustomFieldDto();
 		dto.setJiraKey("TEST-1");
 		dto.setId("testid");
 		dto.setName("TEST Name");
 		dto.setType("type");
 		dto.setValue("value");
 		return dto;
 	}
 
 	protected void clearTestData() throws Exception {
 		jiraCustomFieldDao.deleteAllByJiraKey("TEST-%");
 	}
 
 	@Before
 	public void setup() throws Exception {
 		clearTestData();
 	}
 
 	@After
 	public void teardown() throws Exception {
 		clearTestData();
 	}
 
 	@Test
 	public void test_getAll_DoesNotThrow() {
 		List<JiraCustomFieldDto> dtos = jiraCustomFieldDao.getAll();
 		assertNotNull(dtos);
 	}
 
 	@Test
 	public void test_getByJiraKey_BadId_ReturnsNoResults() {
 		List<JiraCustomFieldDto> dtos = jiraCustomFieldDao
 				.getAllByJiraKey("BADID");
 		assertTrue(dtos == null || dtos.size() == 0);
 	}
 
 	@Test
	public void test_create_TestIssue_Created() {
 		JiraCustomFieldDto dto = getTestJiraCustomFieldDto();
 		assertTrue(jiraCustomFieldDao.create(dto) == 1);
 
 		JiraCustomFieldDto dto2 = getTestJiraCustomFieldDto();
		dto2.setId("id2");
 		dto2.setName("name2");
 		assertTrue(jiraCustomFieldDao.create(dto2) == 1);
 
 		List<JiraCustomFieldDto> readDtos = jiraCustomFieldDao
 				.getAllByJiraKey(dto.getJiraKey());
 
 		assertNotNull(readDtos);
 		assertEquals(2, readDtos.size());
 		assertEquals(dto, readDtos.get(0));
 		assertEquals(dto2, readDtos.get(1));
 	}
 
 	@Test
	public void test_update_TestIssue_IsUpdated() {
 		JiraCustomFieldDto dto = getTestJiraCustomFieldDto();
 		assertTrue(jiraCustomFieldDao.create(dto) == 1);
 
 		dto.setValue("New Value");
 		dto.setType("New Type");
 		dto.setName("New Name");
 		assertEquals(1, jiraCustomFieldDao.update(dto));
 
 		List<JiraCustomFieldDto> readDtos = jiraCustomFieldDao
 				.getAllByJiraKey(dto.getJiraKey());
 
 		boolean found = false;
 		for (JiraCustomFieldDto readDto : readDtos) {
 			if (readDto.getId().equals(dto.getId())) {
 				found = true;
 				assertEquals(dto, readDto);
 
 			}
 		}
 		assertTrue(found);
 	}
 
 }
