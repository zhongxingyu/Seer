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
 
 import com.gordcorp.jira2db.persistence.dto.JiraIssueDto;
 
 public class JiraIssueDaoTest {
 	static JiraIssueDao jiraIssueDao = null;
 
 	static {
 		try {
 			jiraIssueDao = new JiraIssueDao(JiraIssueDto.class,
 					SqlSessionFactorySingleton.instance());
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static JiraIssueDto getTestJiraIssueDto() {
 		JiraIssueDto dto = new JiraIssueDto();
 		dto.setJiraKey("TEST-1");
 		dto.setSummary("TEST SUMMARY");
		dto.setStatus("OPEN");
 		return dto;
 	}
 
 	protected void clearTestData() throws Exception {
 		jiraIssueDao.deleteByJiraKey("TEST-%");
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
 	public void testGetAll_DoesNotThrow() {
 		List<JiraIssueDto> dtos = jiraIssueDao.getAll();
 		assertNotNull(dtos);
 	}
 
 	@Test
 	public void testGet_BadId_ReturnsNull() {
 		JiraIssueDto dto = jiraIssueDao.get(-1);
 		assertNull(dto);
 	}
 
 	@Test
 	public void testCreate_TestIssue_ReturnsId() {
 		JiraIssueDto dto = getTestJiraIssueDto();
 		assertTrue(jiraIssueDao.create(dto) == 1);
 
 		assertTrue(dto.getId() > 0);
 		JiraIssueDto readDto = jiraIssueDao.get(dto.getId());
 		assertNotNull(readDto);
 		assertEquals(dto.getId(), readDto.getId());
 		assertEquals(dto.getJiraKey(), readDto.getJiraKey());
 		assertEquals(dto.getSummary(), readDto.getSummary());
		assertEquals(dto.getStatus(), readDto.getStatus());
 	}
 
 	@Test
 	public void testUpdate_TestIssue_IsUpdated() {
 		JiraIssueDto dto = getTestJiraIssueDto();
 		assertTrue(jiraIssueDao.create(dto) == 1);
 
 		assertTrue(dto.getId() > 0);
 		dto.setSummary("New Summary");
 		assertTrue(jiraIssueDao.update(dto) == 1);
 
 		JiraIssueDto readDto = jiraIssueDao.get(dto.getId());
 		assertNotNull(readDto);
 		assertEquals(dto.getId(), readDto.getId());
 		assertEquals(dto.getJiraKey(), readDto.getJiraKey());
 		assertEquals(dto.getSummary(), readDto.getSummary());
		assertEquals(dto.getStatus(), readDto.getStatus());
 	}
 
 	@Test
 	public void testUpdateByKey_TestIssue_IsUpdated() {
 		JiraIssueDto dto = getTestJiraIssueDto();
 		assertTrue(jiraIssueDao.create(dto) == 1);
 
 		assertTrue(dto.getId() > 0);
 		dto.setId(null);
 		dto.setSummary("New Summary");
 		assertTrue(jiraIssueDao.updateByJiraKey(dto) == 1);
 
 		JiraIssueDto readDto = jiraIssueDao.getByJiraKey(dto.getJiraKey());
 		assertNotNull(readDto);
 		assertEquals(dto.getJiraKey(), readDto.getJiraKey());
 		assertEquals(dto.getSummary(), readDto.getSummary());
		assertEquals(dto.getStatus(), readDto.getStatus());
 	}
 
 }
