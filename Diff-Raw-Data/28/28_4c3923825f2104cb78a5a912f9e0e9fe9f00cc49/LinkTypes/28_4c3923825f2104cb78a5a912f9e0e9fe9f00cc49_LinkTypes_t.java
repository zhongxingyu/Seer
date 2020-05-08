 /**
  * 
  */
 package org.cotrix.web.common.server.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.cotrix.domain.codelist.CodelistLink;
 import org.cotrix.domain.links.AttributeLink;
 import org.cotrix.domain.links.LinkOfLink;
 import org.cotrix.domain.links.NameLink;
 import org.cotrix.domain.links.LinkValueType;
 import org.cotrix.domain.utils.AttributeTemplate;
 import org.cotrix.domain.values.ValueFunction;
 import org.cotrix.domain.values.ValueFunctions.CustomFunction;
 import org.cotrix.domain.values.ValueFunctions.PrefixFunction;
 import org.cotrix.domain.values.ValueFunctions.SuffixFunction;
 import org.cotrix.web.common.shared.codelist.UIAttribute;
 import org.cotrix.web.common.shared.codelist.UICodelist;
 import org.cotrix.web.common.shared.codelist.UIQName;
 import org.cotrix.web.common.shared.codelist.linktype.AttributeValue;
 import org.cotrix.web.common.shared.codelist.linktype.CodeNameValue;
 import org.cotrix.web.common.shared.codelist.linktype.LinkValue;
 import org.cotrix.web.common.shared.codelist.linktype.UILinkType;
 import org.cotrix.web.common.shared.codelist.linktype.UIValueFunction;
 import org.cotrix.web.common.shared.codelist.linktype.UILinkType.UIValueType;
 import org.cotrix.web.common.shared.codelist.linktype.UIValueFunction.Function;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class LinkTypes {
 	
 	private static final CodeNameValue CODE_NAME_TYPE = new CodeNameValue();
 	private static final ArrayList<String> EMPTY_ARGUMENTS = new ArrayList<>();
 	
 	public static UILinkType toUILinkType(CodelistLink codelistLink) {
 		UILinkType linkType = new UILinkType();
 		
		linkType.setId(codelistLink.id());
		
 		UIQName name = ValueUtils.safeValue(codelistLink.name());
 		linkType.setName(name);
 		
 		UICodelist targetCodelist = Codelists.toUICodelist(codelistLink.target());
 		linkType.setTargetCodelist(targetCodelist);
 		
 		UIValueType valueType = toValueType(codelistLink.valueType());
 		linkType.setValueType(valueType);
 		
 		UIValueFunction valueFunction = toValueFunction(codelistLink.function());
 		linkType.setValueFunction(valueFunction);
 		
 		List<UIAttribute> attributes = Codelists.toUIAttributes(codelistLink.attributes());
 		linkType.setAttributes(attributes);
 		
 		return linkType;
 	}
 	
 	private static UIValueType toValueType(LinkValueType valueType) {
 		
 		if (valueType instanceof NameLink) {
 			return CODE_NAME_TYPE;
 		}
 		
 		if (valueType instanceof AttributeLink) {
 			AttributeLink attributeLink = (AttributeLink)valueType;
 			AttributeTemplate template = attributeLink.template();
 			UIQName name = ValueUtils.safeValue(template.name());
 			UIQName type = ValueUtils.safeValue(template.type());
 			String language = ValueUtils.safeValue(template.language());
 			return new AttributeValue(name, type, language);
 		}
 
 		if (valueType instanceof LinkOfLink) {
 			LinkOfLink linkOfLink = (LinkOfLink)valueType;
 			CodelistLink link = linkOfLink.target();
 			String id = link.id();
 			UIQName name = ValueUtils.safeValue(link.name());
 			return new LinkValue(id, name);
 		}
 		
 		throw new IllegalArgumentException("Unknown value type :"+valueType);
 	}
 	
 	public static LinkValue toLinkType(CodelistLink codelistLink) {
 		return new LinkValue(codelistLink.id(), ValueUtils.safeValue(codelistLink.name()));
 	}
 	
 	private static UIValueFunction toValueFunction(ValueFunction valueFunction) {
 		switch (valueFunction.name()) {
 			case "identity": return new UIValueFunction(Function.IDENTITY, EMPTY_ARGUMENTS);
 			case "uppercase": return new UIValueFunction(Function.UPPERCASE, EMPTY_ARGUMENTS);
 			case "lowercase": return new UIValueFunction(Function.LOWERCASE, EMPTY_ARGUMENTS);
 			case "prefix": return new UIValueFunction(Function.PREFIX, singleArgument(((PrefixFunction)valueFunction).prefix()));
 			case "suffix": return new UIValueFunction(Function.SUFFIX, singleArgument(((SuffixFunction)valueFunction).suffix()));
 			case "generic": return new UIValueFunction(Function.CUSTOM, singleArgument(((CustomFunction)valueFunction).expression()));
 		}
 		throw new IllegalArgumentException("Unknown value function "+valueFunction);
 		
 	}
 	
 	private static List<String> singleArgument(String argument) {
 		ArrayList<String> arguments = new ArrayList<>();
 		arguments.add(argument);
 		return arguments;
 	}
 }
