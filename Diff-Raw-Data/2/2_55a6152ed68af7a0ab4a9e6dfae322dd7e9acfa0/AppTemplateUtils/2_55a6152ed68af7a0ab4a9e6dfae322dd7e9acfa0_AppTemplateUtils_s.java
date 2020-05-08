 package org.iplantc.de.client.util;
 
 import org.iplantc.de.client.models.apps.integration.AppTemplate;
 import org.iplantc.de.client.models.apps.integration.AppTemplateAutoBeanFactory;
 import org.iplantc.de.client.models.apps.integration.Argument;
 import org.iplantc.de.client.models.apps.integration.ArgumentGroup;
 import org.iplantc.de.client.models.apps.integration.ArgumentType;
 import org.iplantc.de.client.models.apps.integration.SelectionItem;
 import org.iplantc.de.client.models.apps.integration.SelectionItemGroup;
 import org.iplantc.de.resources.client.messages.I18N;
 import org.iplantc.de.resources.client.uiapps.widgets.AppsWidgetsDisplayMessages;
 
 import com.google.common.collect.Lists;
 import com.google.gwt.core.client.GWT;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.google.web.bindery.autobean.shared.AutoBeanUtils;
 import com.google.web.bindery.autobean.shared.Splittable;
 import com.google.web.bindery.autobean.shared.impl.StringQuoter;
 
 import java.util.Collections;
 import java.util.List;
 
 public class AppTemplateUtils {
     private static final AppTemplateAutoBeanFactory factory = GWT.create(AppTemplateAutoBeanFactory.class);
     private static final AppsWidgetsDisplayMessages displayMessages = I18N.APPS_MESSAGES;
 
     private static final String SELECTION_ITEM_GROUP_ARGUMENTS = "arguments";
     private static final String SELECTION_ITEM_GROUP_GROUPS = "groups";
     private static final String SELECTION_ITEM_GROUP_SINGLE_SELECT = "isSingleSelect";
     private static final String SELECTION_ITEM_GROUP_CASCASE = "selectionCascade";
     private static Argument EMPTY_GROUP_ARG;
 
     public static final String EMPTY_GROUP_ARG_ID = "emptyArgumentGroupInfoArgumentId"; //$NON-NLS-1$
     public static final String NEW_ENV_VAR_NAME = "NEW_ENV_VAR"; //$NON-NLS-1$
 
     public static Argument getEmptyGroupArgument() {
         if (EMPTY_GROUP_ARG == null) {
             EMPTY_GROUP_ARG = factory.argument().as();
             EMPTY_GROUP_ARG.setId(EMPTY_GROUP_ARG_ID);
             EMPTY_GROUP_ARG.setType(ArgumentType.Info);
             EMPTY_GROUP_ARG.setLabel(displayMessages.emptyArgumentGroupBgText());
             EMPTY_GROUP_ARG.setDescription(displayMessages.emptyArgumentGroupBgText());
         }
         return EMPTY_GROUP_ARG;
     }
 
     public static AppTemplate removeEmptyGroupArguments(final AppTemplate at) {
         AppTemplate copy = copyAppTemplate(at);
         for (ArgumentGroup ag : copy.getArgumentGroups()) {
             List<Argument> arguments = ag.getArguments();
             if ((arguments.size() == 1) && (arguments.get(0).getId() != null) && (arguments.get(0).getId().equals(EMPTY_GROUP_ARG_ID))) {
                 arguments.clear();
             }
         }
         return copy;
     }
 
     public static AppTemplate copyAppTemplate(AppTemplate value) {
         AutoBean<AppTemplate> argAb = AutoBeanUtils.getAutoBean(value);
         Splittable splitCopy = AutoBeanCodex.encode(argAb);
 
         AppTemplate ret = AutoBeanCodex.decode(factory, AppTemplate.class, splitCopy.getPayload()).as();
 
         // Copy the arguments lists back in.
         for (int i = 0; i < value.getArgumentGroups().size(); i++) {
             ArgumentGroup agOrig = value.getArgumentGroups().get(i);
             ArgumentGroup agNew = ret.getArgumentGroups().get(i);
             for (int j = 0; j < agOrig.getArguments().size(); j++) {
                 Argument argOrig = agOrig.getArguments().get(j);
                 Argument argNew = agNew.getArguments().get(j);
                 argNew.setSelectionItems(argOrig.getSelectionItems());
             }
         }
 
         return ret;
     }
 
     public static Argument copyArgument(Argument value) {
         AutoBean<Argument> argAb = AutoBeanUtils.getAutoBean(value);
         Splittable splitCopy = AutoBeanCodex.encode(argAb);
 
         return AutoBeanCodex.decode(factory, Argument.class, splitCopy.getPayload()).as();
     }
 
     public static ArgumentGroup copyArgumentGroup(ArgumentGroup value) {
         AutoBean<ArgumentGroup> argGrpAb = AutoBeanUtils.getAutoBean(value);
         Splittable splitCopy = AutoBeanCodex.encode(argGrpAb);
 
         ArgumentGroup ret = AutoBeanCodex.decode(factory, ArgumentGroup.class, splitCopy).as();
         if (ret.getArguments() == null) {
             ret.setArguments(Collections.<Argument> emptyList());
         }
         return ret;
     }
 
     public static Argument reserializeTreeSelectionArguments(Argument arg) {
         if (!arg.getType().equals(ArgumentType.TreeSelection)) {
             return arg;
         }
         // JDS Sanity check, there should only be one item in the list, and it should have the following
         // keys. Also, fail fast.
         Splittable firstItemCheck = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(arg.getSelectionItems().get(0)));
         if ((arg.getSelectionItems().size() == 1)
                 && firstItemCheck.getPropertyKeys().contains(SELECTION_ITEM_GROUP_CASCASE)
                 && firstItemCheck.getPropertyKeys().contains(SELECTION_ITEM_GROUP_SINGLE_SELECT)) {
             arg.setSelectionItems(recastSelectionItems(arg.getSelectionItems()));
         }
         return arg;
     }
 
     /**
      * Determines if the given items are equal by serializing them and comparing their
      * <code>Splittable</code> payloads.
      * 
      * @param a
      * @param b
      * @return
      */
     public static boolean areEqual(SelectionItem a, SelectionItem b) {
         Splittable aSplit = getSplittable(AutoBeanUtils.getAutoBean(a));
         Splittable bSplit = getSplittable(AutoBeanUtils.getAutoBean(b));
         return aSplit.getPayload().equals(bSplit.getPayload());
     }
     
     public static boolean isSelectionArgumentType(ArgumentType type) {
         return isSimpleSelectionArgumentType(type) || type.equals(ArgumentType.TreeSelection);
     }
     
     public static boolean isSimpleSelectionArgumentType(ArgumentType t) {
         return t.equals(ArgumentType.TextSelection)
                 || t.equals(ArgumentType.IntegerSelection)
                 || t.equals(ArgumentType.DoubleSelection)
                 || t.equals(ArgumentType.Selection)
                 || t.equals(ArgumentType.ValueSelection);
     }
 
     private static Splittable getSplittable(AutoBean<?> autoBean) {
         return AutoBeanCodex.encode(autoBean);
     }
 
     /**
      * Takes a list of {@link SelectionItem}s and re-serializes them to {@link SelectionItemGroup}s as
      * necessary.
      * This is to provide some level of type safety for the operations in Hierarchical List Fields and
      * Editors (e.g. for instanceof checks).
      * 
      * @param selectionItems the list of {@linkplain SelectionItem}s to be operated on.
      * @return
      */
     private static List<SelectionItem> recastSelectionItems(List<SelectionItem> selectionItems) {
         if ((selectionItems == null) || selectionItems.isEmpty()) {
             return Collections.<SelectionItem> emptyList();
         }
 
         List<SelectionItem> selectionItemsRet = Lists.newArrayList();
         for (SelectionItem si : selectionItems) {
             Splittable siSplit = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(si));
             // JDS Check keys.
             List<String> propertyKeys = siSplit.getPropertyKeys();
             if (propertyKeys.contains(SELECTION_ITEM_GROUP_ARGUMENTS)
                     || propertyKeys.contains(SELECTION_ITEM_GROUP_GROUPS)
                     || propertyKeys.contains(SELECTION_ITEM_GROUP_SINGLE_SELECT)
                     || propertyKeys.contains(SELECTION_ITEM_GROUP_CASCASE)) {
                 // JDS Re-serialize the node as a SelectionItemGroup and add it to the return list.
                 SelectionItemGroup sig = AutoBeanCodex.decode(factory, SelectionItemGroup.class, siSplit).as();
                 selectionItemsRet.add(sig);
             } else {
                 selectionItemsRet.add(si);
             }
         }
 
         // JDS Sanity check. Not sure what to do if this is actually met.
         if (selectionItemsRet.size() != selectionItems.size()) {
             GWT.log("HOUSTON!!! WE HAVE A PROBLEM!!!");
             return null;
         }
         return selectionItemsRet;
 
     }
 
     public static boolean isDiskResourceArgumentType(ArgumentType type) {
        return type.equals(ArgumentType.FileInput) || type.equals(ArgumentType.FolderInput) || type.equals(ArgumentType.MultiFileSelector) || isDiskResourceOutputType(type);
     }
 
     public static boolean isDiskResourceOutputType(ArgumentType type) {
         return type.equals(ArgumentType.FileOutput)
                 || type.equals(ArgumentType.FolderOutput) || type.equals(ArgumentType.MultiFileOutput);
     }
 
     public static boolean isTextType(ArgumentType type) {
         return type.equals(ArgumentType.Text) || type.equals(ArgumentType.MultiLineText) || type.equals(ArgumentType.EnvironmentVariable) || type.equals(ArgumentType.Output)
                 || type.equals(ArgumentType.Number) || type.equals(ArgumentType.Integer) || type.equals(ArgumentType.Double);
     }
 
     public static boolean isReferenceType(ArgumentType type) {
         return type.equals(ArgumentType.ReferenceAnnotation) || type.equals(ArgumentType.ReferenceGenome) || type.equals(ArgumentType.ReferenceSequence);
     }
 
     public static boolean typeSupportsValidators(ArgumentType type) {
         return type.equals(ArgumentType.Text) || type.equals(ArgumentType.Double) || type.equals(ArgumentType.Integer);
     }
 
     public static List<SelectionItem> getSelectedTreeItems(SelectionItemGroup sig) {
         if ((sig == null) || (sig.getArguments() == null)) {
             return Collections.emptyList();
         }
         List<SelectionItem> ret = Lists.newArrayList();
         for (SelectionItem si : sig.getArguments()) {
             if (si.isDefault()) {
                 ret.add(si);
             }
         }
 
         if (sig.getGroups() != null) {
             for (SelectionItemGroup subSig : sig.getGroups()) {
                 ret.addAll(getSelectedTreeItems(subSig));
             }
         }
 
         return ret;
     }
 
     public static Splittable getSelectedTreeItemsAsSplittable(SelectionItemGroup sig) {
         List<SelectionItem> selectedItems = getSelectedTreeItems(sig);
         Splittable splitArr = StringQuoter.createIndexed();
         int i = 0;
         for (SelectionItem si : selectedItems) {
             Splittable siSplit = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(si));
             siSplit.assign(splitArr, i++);
         }
 
         return splitArr;
     }
 
     public static SelectionItemGroup selectionItemToSelectionItemGroup(SelectionItem selectionItem) {
         Splittable split = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(selectionItem));
         SelectionItemGroup ret = AutoBeanCodex.decode(factory, SelectionItemGroup.class, split).as();
         return ret;
     }
 
 }
