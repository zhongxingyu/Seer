 package org.safaproject.safa.dao;
 
 import static org.junit.Assert.assertEquals;
 
import javax.validation.ConstraintViolationException;

 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.safaproject.safa.model.tag.TagDataTypes;
 import org.safaproject.safa.model.tag.TagType;
 import org.safaproject.safa.model.tag.builder.TagTypeBuilder;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath:safa-unit-test-context.xml")
 @Transactional
 public class TagTypeDAOTest {
 
 	@Autowired
 	private TagTypeDAO tagTypeDAO;
 
 	@Test
 	public void shallCreateTagType() {
 
 		TagType tagType = new TagTypeBuilder().withTagName("Teacher")
 				.withTagDataType(TagDataTypes.STRING).build();
 		tagTypeDAO.save(tagType);
 
 		TagType tagTypeFromDB = tagTypeDAO.findById(tagType.getTagName());
 
 		assertEquals(tagType, tagTypeFromDB);
 	}
 
 	@Test
 	public void shallFindAll() {
 
 		TagType tagType1 = new TagTypeBuilder().withTagName("Teacher")
 				.withTagDataType(TagDataTypes.STRING).build();
 		tagTypeDAO.save(tagType1);
 
 		TagType tagType2 = new TagTypeBuilder().withTagName("Exam Date")
 				.withTagDataType(TagDataTypes.DATE).build();
 		tagTypeDAO.save(tagType2);
 
 		assertEquals(tagTypeDAO.findAll().size(), 2);
 	}
 
 	@Test
 	public void shallFindByExample() {
 		TagType tagType = new TagTypeBuilder().withTagName("University")
 				.withTagDataType(TagDataTypes.STRING).build();
 		tagTypeDAO.save(tagType);
 
 		TagType example = new TagType();
 		example.setTagName("University");
 
 		TagType tagTypeFromDB = tagTypeDAO.findByExample(example).get(0);
 
 		assertEquals(tagType, tagTypeFromDB);
 	}
 
 	@Test
 	public void shallGetTagsTypeCount() {
 		TagType tagType1 = new TagTypeBuilder().withTagName("University")
 				.withTagDataType(TagDataTypes.STRING).build();
 		tagTypeDAO.save(tagType1);
 
 		TagType tagType2 = new TagTypeBuilder().withTagName("Exam Date")
 				.withTagDataType(TagDataTypes.DATE).build();
 		tagTypeDAO.save(tagType2);
 
 		assertEquals(tagTypeDAO.countAll(), new Long(2));
 	}
 
 	@Test
 	public void shallCountByExample() {
 		TagType tagType1 = new TagTypeBuilder().withTagName("University")
 				.withTagDataType(TagDataTypes.STRING).build();
 		tagTypeDAO.save(tagType1);
 
 		TagType tagType2 = new TagTypeBuilder().withTagName("Exam Date")
 				.withTagDataType(TagDataTypes.DATE).build();
 		tagTypeDAO.save(tagType2);
 
 		TagType example = new TagType();
 
 		example.setTagName("Exam Date");
 		example.setTagDataType(TagDataTypes.STRING);
 
 		assertEquals(tagTypeDAO.countByExample(example), new Long(1));
 	}
 
 	@Test
 	public void shallDeleteTagType() {
 		TagType tagType1 = new TagTypeBuilder().withTagName("University")
 				.withTagDataType(TagDataTypes.STRING).build();
 		tagTypeDAO.save(tagType1);
 
 		TagType tagType2 = new TagTypeBuilder().withTagName("Exam Date")
 				.withTagDataType(TagDataTypes.DATE).build();
 		tagTypeDAO.save(tagType2);
 
 		tagTypeDAO.delete(tagType1);
 
 		assertEquals(tagTypeDAO.countAll(), new Long(1));
 
 		tagTypeDAO.delete(tagType2);
 
 		assertEquals(tagTypeDAO.countAll(), new Long(0));
 
 	}
 
 	@Test
 	public void updateTest() {
 		TagType tagType1 = new TagTypeBuilder().withTagName("University")
 				.withTagDataType(TagDataTypes.STRING).build();
 		tagTypeDAO.save(tagType1);
 
 		assertEquals(tagTypeDAO.countAll(), new Long(1));
 
 		TagType toUpdate = tagTypeDAO.findById(tagType1.getTagName());
 
 		/**
 		 * The TagType Name cannot be changed/updated because it is the ID of
 		 * the entity
 		 */
 
 		toUpdate.setTagDataType(TagDataTypes.NUMERIC);
 		toUpdate = tagTypeDAO.update(toUpdate);
 
 		assertEquals(tagTypeDAO.countAll(), new Long(1));
 
 		TagType updated = tagTypeDAO.findById(toUpdate.getTagName());
 
 		assertEquals(TagDataTypes.NUMERIC, updated.getTagDataType());
 
 	}
 
	@Test(expected = ConstraintViolationException.class)
 	public void shallFailBecauseOfNullDataType() {
 		TagType tagType = new TagType();
 		tagType.setTagName("Career");
 		tagType.setTagDataType(null);
 		tagTypeDAO.save(tagType);
 	}
 
 }
