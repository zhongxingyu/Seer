 /**
  * 
  */
 package org.cotrix.web.manage.server.modify;
 
 import static org.cotrix.domain.dsl.Codes.*;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.namespace.QName;
 
 import org.cotrix.domain.attributes.Attribute;
 import org.cotrix.domain.attributes.Definition;
 import org.cotrix.domain.codelist.Code;
 import org.cotrix.domain.codelist.Codelink;
 import org.cotrix.domain.codelist.Codelist;
 import org.cotrix.domain.codelist.CodelistLink;
 import org.cotrix.domain.common.NamedContainer;
 import org.cotrix.domain.common.Range;
 import org.cotrix.domain.common.Ranges;
 import org.cotrix.domain.dsl.grammar.CodelistLinkGrammar.OptionalClause;
 import org.cotrix.domain.validation.Validator;
 import org.cotrix.domain.validation.Validators;
 import org.cotrix.domain.values.DefaultType;
 import org.cotrix.domain.values.ValueFunction;
 import org.cotrix.domain.values.ValueFunctions;
 import org.cotrix.domain.values.ValueType;
 import org.cotrix.web.common.shared.Language;
 import org.cotrix.web.common.shared.codelist.UIAttribute;
 import org.cotrix.web.common.shared.codelist.UICode;
 import org.cotrix.web.common.shared.codelist.UILink;
 import org.cotrix.web.common.shared.codelist.UIQName;
 import org.cotrix.web.common.shared.codelist.attributetype.UIAttributeType;
 import org.cotrix.web.common.shared.codelist.attributetype.UIConstraint;
 import org.cotrix.web.common.shared.codelist.attributetype.UIRange;
 import org.cotrix.web.common.shared.codelist.linktype.AttributeValue;
 import org.cotrix.web.common.shared.codelist.linktype.CodeNameValue;
 import org.cotrix.web.common.shared.codelist.linktype.LinkValue;
 import org.cotrix.web.common.shared.codelist.linktype.UILinkType;
 import org.cotrix.web.common.shared.codelist.linktype.UILinkType.UIValueType;
 import org.cotrix.web.common.shared.codelist.linktype.UIValueFunction;
import org.cotrix.web.manage.client.util.Attributes;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  * 
  */
 public class ChangesetUtil {
 
 	public static Attribute addAttribute(UIAttribute uiAttribute) {
 		return attribute().name(convert(uiAttribute.getName())).value(convert(uiAttribute.getValue()))
 				.ofType(convert(uiAttribute.getType())).in(convert(uiAttribute.getLanguage())).build();
 	}
 
 	public static Attribute updateAttribute(UIAttribute uiAttribute) {
 		return modifyAttribute(uiAttribute.getId()).name(convert(uiAttribute.getName()))
 				.value(convert(uiAttribute.getValue())).ofType(convert(uiAttribute.getType()))
 				.in(convert(uiAttribute.getLanguage())).build();
 	}
 
 	public static Attribute removeAttribute(String id) {
 		return deleteAttribute(id);
 	}
 
 	public static List<Attribute> addAttributes(List<UIAttribute> uiAttributes) {
 		if (uiAttributes.size() == 0)
 			return Collections.emptyList();
 		List<Attribute> attributes = new ArrayList<Attribute>(uiAttributes.size());
 		for (UIAttribute uiAttribute : uiAttributes)
 			attributes.add(addAttribute(uiAttribute));
 		return attributes;
 	}
 
 	public static Code addCode(UICode uicode) {
 		// FIXME tmp workaround
 		if (uicode.getAttributes() == null || uicode.getAttributes().isEmpty())
 			return code().name(convert(uicode.getName())).build();
 		else
 			return code().name(convert(uicode.getName())).attributes(addAttributes(uicode.getAttributes())).build();
 	}
 
 	public static Code updateCode(String id, UIQName name) {
 		return modifyCode(id).name(convert(name)).build();
 	}
 
 	public static Code removeCode(String id) {
 		return deleteCode(id);
 	}
 
 	public static Codelist updateCodelist(String id, UIQName name) {
 		return modifyCodelist(id).name(convert(name)).build();
 	}
 	
 	public static Codelink addCodelink(UILink link, CodelistLink linkType, Code code) {
 		List<Attribute> attributes = addAttributes(link.getAttributes());
 		return link().instanceOf(linkType).target(code).attributes(attributes).build();
 	}
 	
 	public static Codelink updateCodelink(UILink link, Codelink oldLink, CodelistLink linkType, Code code) {
 		List<Attribute> attributes = buildChangeSet(link.getAttributes(), oldLink.attributes());
 		return modifyLink(link.getId()).target(code).attributes(attributes).build();
 	}
 	
 	public static Codelink removeCodelink(UILink link) {
 		return deleteLink(link.getId());
 	}
 	
 	public static CodelistLink addCodelistLink(UILinkType linkType, Codelist target) {
 		UIValueType valueType = linkType.getValueType();
 		
 		OptionalClause clause = null;
 		
 		if (valueType instanceof CodeNameValue) {
 			clause = listLink().name(convert(linkType.getName())).target(target).anchorToName();
 		}
 		
 		if (valueType instanceof AttributeValue) {
 			AttributeValue attributeType = (AttributeValue) valueType;
 			Attribute template = toAttribute(attributeType);
 			clause = listLink().name(convert(linkType.getName())).target(target).anchorTo(template);
 		}
 		
 		if (valueType instanceof LinkValue) {
 			LinkValue link = (LinkValue) valueType;
 			CodelistLink codelistLink = findCodelistLink(link.getLinkId(), target.links());
 			clause = listLink().name(convert(linkType.getName())).target(target).anchorTo(codelistLink);
 		}
 		
 		if (clause == null) throw new IllegalArgumentException("Unsupported value type "+valueType);
 		
 		List<Attribute> attributes = addAttributes(linkType.getAttributes());
 		clause = clause.attributes(attributes);
 		
 		if (linkType.getValueFunction()!=null) {
 			ValueFunction function = toValueFunction(linkType.getValueFunction());
 			clause =clause.transformWith(function);
 		}
 		
 		return clause.build();
 	}
 	
 	private static ValueFunction toValueFunction(UIValueFunction valueFunction) {
 		switch (valueFunction.getFunction()) {
 			case IDENTITY: return ValueFunctions.identity;
 			case CUSTOM: return ValueFunctions.custom(getFirstArgument(valueFunction.getArguments()));
 			case LOWERCASE: return ValueFunctions.lowercase;
 			case PREFIX: return ValueFunctions.prefix(getFirstArgument(valueFunction.getArguments()));
 			case SUFFIX: return ValueFunctions.suffix(getFirstArgument(valueFunction.getArguments()));
 			case UPPERCASE: return ValueFunctions.uppercase;
 			default: throw new IllegalArgumentException("unknown function "+valueFunction);
 		}
 	}
 	
 	private static String getFirstArgument(List<String> arguments) {
 		if (arguments.size()<1) throw new IllegalArgumentException("Missing arguments, found "+arguments.size()+" expected 1");
 		return arguments.get(0);
 	}
 
 	public static CodelistLink updateCodelistLink(UILinkType linkType, Codelist target, CodelistLink oldCodelistLink) {
 		
 		UIValueType valueType = linkType.getValueType();
 		
 		OptionalClause clause = null;
 		
 		if (valueType instanceof CodeNameValue) {
 			clause = modifyListLink(linkType.getId()).name(convert(linkType.getName())).anchorToName();
 		}
 		
 		if (valueType instanceof AttributeValue) {
 			AttributeValue attributeType = (AttributeValue) valueType;
 			Attribute template = toAttribute(attributeType);
 			clause = modifyListLink(linkType.getId()).name(convert(linkType.getName())).anchorTo(template);
 		}
 		
 		if (valueType instanceof LinkValue) {
 			LinkValue link = (LinkValue) valueType;
 			CodelistLink codelistLink = findCodelistLink(link.getLinkId(), target.links());
 			clause = modifyListLink(linkType.getId()).name(convert(linkType.getName())).anchorTo(codelistLink);
 		}
 		
 		if (clause == null) throw new IllegalArgumentException("Unsupported value type "+valueType);
 		
 		List<Attribute> attributes = buildChangeSet(linkType.getAttributes(), oldCodelistLink.attributes());
 		clause = clause.attributes(attributes);
 		
 		if (linkType.getValueFunction()!=null) {
 			ValueFunction function = toValueFunction(linkType.getValueFunction());
 			clause = clause.transformWith(function);
 		}
 		
 		return clause.build();
 	}
 	
 	private static List<Attribute> buildChangeSet(List<UIAttribute> uiAttributes, NamedContainer<? extends Attribute> oldAttributes) {
 		
 		List<Attribute> changeSet = new ArrayList<>();
 		
 		Set<String> updatedAttributeIds = new HashSet<>();
 		
 		for (UIAttribute uiAttribute:uiAttributes) {
 			
 			//skip system attributes
			if (Attributes.isSystemAttribute(uiAttribute)) continue;
 		
 			//is a new attribute
 			if (uiAttribute.getId() == null) changeSet.add(addAttribute(uiAttribute));
 			//is an updated attribute
 			else {
 				changeSet.add(updateAttribute(uiAttribute));
 				updatedAttributeIds.add(uiAttribute.getId());
 			}
 		}
 		
 		for (Attribute oldAttribute:oldAttributes) {
 			String id = oldAttribute.id();
 			if (!updatedAttributeIds.contains(id)) changeSet.add(removeAttribute(id));
 		}
 				
 		return changeSet;
 	}
 	
 	private static Attribute toAttribute(AttributeValue attributeType) {
 		return attribute().name(convert(attributeType.getName()))
 				.ofType(convert(attributeType.getType())).in(convert(attributeType.getLanguage())).build();
 	}
 	
 	private static CodelistLink findCodelistLink(String id, NamedContainer<? extends CodelistLink> namedContainer) {
 		for (CodelistLink codelistLink:namedContainer) if (codelistLink.id().equals(id)) return codelistLink;
 		throw new IllegalArgumentException("Unknown codelist type with id: "+id);
 	}
 
 	public static CodelistLink removeCodelistLink(String id) {
 		return deleteListLink(id);
 	}
 	
 	
 	
 	public static Definition addDefinition(UIAttributeType attributeType) {
 		return definition().name(convert(attributeType.getName()))
 				.is(convert(attributeType.getType()))
 				.in(convert(attributeType.getLanguage()))
 				.occurs(toRange(attributeType.getRange()))
 				.valueIs(toValueType(attributeType.getDefaultValue(), attributeType.getConstraints()))
 				.build();
 	}
 	
 	public static Definition updateDefinition(UIAttributeType attributeType) {
 		return modifyDefinition(attributeType.getId()).name(convert(attributeType.getName()))
 				.is(convert(attributeType.getType()))
 				.in(convert(attributeType.getLanguage()))
 				.occurs(toRange(attributeType.getRange()))
 				.valueIs(toValueType(attributeType.getDefaultValue(), attributeType.getConstraints()))
 				.build();
 	}
 	
 	public static Definition removeDefinition(UIAttributeType attributeType) {
 		return deleteDefinition(attributeType.getId());
 	}
 	
 	public static Range toRange(UIRange range) {
 		return Ranges.between(range.getMin(), range.getMax());
 	}
 	
 	public static ValueType toValueType(String defaultValue, List<UIConstraint> constraints) {
 		DefaultType type = valueType();
 		
 		for (UIConstraint constraint:constraints) addConstraint(type, constraint);
 
 		type.defaultsTo(defaultValue);
 		
 		return type;
 	}
 	
 	private static void addConstraint(DefaultType type, UIConstraint constraint) {
 		Validator constraintValidator = null;
 		for (Validator validator:Validators.values()) {
 			if (validator.name().equals(constraint.getName())) {
 				constraintValidator = validator;
 				break;
 			}
 		}
 		if (constraintValidator == null) throw new IllegalArgumentException("Validator for constraint "+constraint+" not found");
 		
 		type.with(constraintValidator.instance(constraint.getParameters().toArray()));
 	}
 	
 	
 	public static String convert(Language language) {
 		return (language == null || language == Language.NONE) ? null : language.getCode();
 	}
 
 	public static String convert(String value) {
 		return (value == null || value.isEmpty()) ? null : value;
 	}
 
 	public static QName convert(UIQName value) {
 		return (value == null || value.getLocalPart() == null || value.getLocalPart().isEmpty()) ? null : new QName(
 				value.getNamespace(), value.getLocalPart());
 	}
 }
