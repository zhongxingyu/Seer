 package com.nanuvem.lom.kernel.dao.memory;
 
 import com.nanuvem.lom.kernel.AttributeValue;
 import com.nanuvem.lom.kernel.dao.AttributeValueDao;
 
 public class MemoryAttributeValueDao implements AttributeValueDao {
 
 	private Long id = 1L;
 
 	public void create(AttributeValue value) {
 		value.setId(id++);
 		value.setVersion(0);
 	}
}
