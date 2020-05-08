 /**
  * 
  */
 package org.cotrix.web.manage.server;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.cotrix.domain.codelist.Code;
 import org.cotrix.domain.codelist.Codelink;
 import org.cotrix.domain.common.Attribute;
 import org.cotrix.domain.common.NamedContainer;
 import org.cotrix.domain.utils.Constants;
 import org.cotrix.web.common.server.util.ValueUtils;
 import org.cotrix.web.manage.shared.AttributeGroup;
 import org.cotrix.web.manage.shared.Group;
 import org.cotrix.web.manage.shared.HasPosition;
 import org.cotrix.web.manage.shared.LinkGroup;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class GroupFactory {
 	
 	
 	private interface ItemProvider<T> {
 		NamedContainer<? extends T> getItems(Code code);
 	}
 	
 	private interface GroupGenerator<T, G extends Group> {
 		G generate(T item);
 	}
 
 	private static final GroupGenerator<Attribute, AttributeGroup> ATTRIBUTE_GROUP_GENERATOR = new GroupGenerator<Attribute, AttributeGroup>() {
 
 		@Override
 		public AttributeGroup generate(Attribute attribute) {
 			boolean isSystemGroup = attribute.type()!=null?attribute.type().equals(Constants.SYSTEM_TYPE):false;
 			return new AttributeGroup(ValueUtils.safeValue(attribute.name()), null, ValueUtils.safeValue(attribute.language()), isSystemGroup);
 		}
 	};
 	
 	private static final ItemProvider<Attribute> ATTRIBUTES_PROVIDER = new ItemProvider<Attribute>() {
 
 		@Override
 		public NamedContainer<? extends Attribute> getItems(Code code) {
 			return code.attributes();
 		}
 	};
 	
 	private static final GroupGenerator<Codelink, LinkGroup> LINK_GROUP_GENERATOR = new GroupGenerator<Codelink, LinkGroup>() {
 
 		@Override
 		public LinkGroup generate(Codelink link) {
 			boolean isSystemGroup = link.type()!=null?link.type().equals(Constants.SYSTEM_TYPE):false;
 			return new LinkGroup(ValueUtils.safeValue(link.name()), isSystemGroup);
 		}
 	};
 	
 	private static final ItemProvider<Codelink> CODELINKS_PROVIDER = new ItemProvider<Codelink>() {
 
 		@Override
 		public NamedContainer<? extends Codelink> getItems(Code code) {
 			return code.links();
 		}
 	};
 	
 	public static List<Group> getGroups(Iterable<Code> codes) {
 		List<Group> groups = getGroups(codes, ATTRIBUTES_PROVIDER, ATTRIBUTE_GROUP_GENERATOR);
 		groups.addAll(getGroups(codes, CODELINKS_PROVIDER, LINK_GROUP_GENERATOR));
 		return groups;
 		
 	}
 		
 	@SuppressWarnings("unchecked")
 	private static <T, G extends Group & HasPosition> List<Group> getGroups(Iterable<Code> codes, ItemProvider<T> itemProvider, GroupGenerator<T, G> groupGenerator)
 	{
 		Map<G, Integer> groupsCounter = new HashMap<G, Integer>();
 		for (Code code:codes) {
 			Map<G, Integer> itemGroupsCounter = new HashMap<G, Integer>();
 			
 			for (T item:itemProvider.getItems(code)) {
 				G group = groupGenerator.generate(item);
 				
 				Integer counter = itemGroupsCounter.get(group);
 				if (counter == null) counter = 0;
 				counter++;
 				itemGroupsCounter.put(group, counter);
 			}
 			for (Entry<G, Integer> itemGroupCounter:itemGroupsCounter.entrySet()) {
 				Integer groupCounter = groupsCounter.get(itemGroupCounter.getKey());
 				groupCounter = groupCounter == null?0:groupCounter;
 				int count = Math.max(groupCounter, itemGroupCounter.getValue());
 				groupsCounter.put(itemGroupCounter.getKey(), count);
 			}
 		}
 		
 		List<Group> groups = new ArrayList<Group>();
 		for (Entry<G, Integer> groupCounter:groupsCounter.entrySet()) {
 			for (int i = 0; i < groupCounter.getValue(); i++) {
				G group = (G) ((G) groupCounter.getKey()).cloneGroup();
 				group.setPosition(i);
 				groups.add(group);
 			}
 		}
 
 		return groups;
 	}
 
 }
