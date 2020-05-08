 package md.frolov.legume.client.service.impl;
 
 import java.util.Map;
 import java.util.Set;
 import javax.annotation.Nonnull;
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.google.gwt.dom.client.StyleElement;
 import com.google.gwt.dom.client.StyleInjector;
import com.google.web.bindery.event.shared.EventBus;
 
 import md.frolov.legume.client.Constants;
 import md.frolov.legume.client.elastic.model.ModelFactory;
import md.frolov.legume.client.events.ColorConfUpdatedEvent;
 import md.frolov.legume.client.model.ColorsConf;
 import md.frolov.legume.client.service.ColorizeService;
 import md.frolov.legume.client.service.ConfigurationService;
 
 /** @author Ivan Frolov (ifrolov@tacitknowledge.com) */
 @Singleton
 public class ColorizeServiceImpl implements ColorizeService, Constants
 {
     @Inject
     private ConfigurationService configurationService;
 
    @Inject
    private EventBus eventBus;

     private StyleElement styleElement;
 
     /** This map contains color mappings: fieldName -> value -> color hue (0 to 360) */
     private final Map<String, Map<String, Integer>> fieldValueColorMap;
     private final Set<String> labelFields;
     private final Set<String> backgroundFields;
 
     @Inject
     public ColorizeServiceImpl(ConfigurationService configurationService)
     {
         ColorsConf colorsConf = configurationService.getObject(COLORS_CONF, ColorsConf.class);
        fieldValueColorMap = Maps.newHashMap();
        labelFields = Sets.newLinkedHashSet();
        backgroundFields = Sets.newLinkedHashSet();
         if(colorsConf!=null) {
            for (Map.Entry<String, Map<String, Integer>> entry : colorsConf.getFieldValueColorMap().entrySet())
            {
                fieldValueColorMap.put(entry.getKey(), Maps.newLinkedHashMap(entry.getValue()));
            }
            labelFields.addAll(colorsConf.getLabelFields());
            backgroundFields.addAll(colorsConf.getBackgroundFields());
         } else {
             addColorizableField("@type");
             labelFields.add("@type");
         }
 
         refresh();
     }
 
     private String generateCssStyleSheet() {
         StringBuilder sb = new StringBuilder();
         for (String labelField : labelFields)
         {
             Map<String, Integer> colors = fieldValueColorMap.get(labelField);
             appendFieldColors(".hlLabel", labelField, colors, 100, 40, sb);
         }
         for (String labelField : backgroundFields)
         {
             Map<String, Integer> colors = fieldValueColorMap.get(labelField);
             appendFieldColors(".hlBackground", labelField, colors, 70, 98, sb);
         }
         return sb.toString();
     }
 
     private void appendFieldColors(String elementType, String fieldName, Map<String, Integer> colors, int saturation, int light,
                                    StringBuilder sb){
         if(colors == null) {
             return;
         }
         for (Map.Entry<String, Integer> color : colors.entrySet())
         {
             String cssClassName = getCssClassName(fieldName, color.getKey());
             String cssColor = getCssColor(color.getValue(), saturation, light);
             sb.append(".").append(cssClassName).append(" ").append(elementType).append("{")
                     .append("background-color:").append(cssColor).append("} ");
         }
     }
 
     @Override
     public void saveAndRefresh() {
         save();
         refresh();
     }
 
     private void save() {
         ColorsConf colorsConf = ModelFactory.INSTANCE.colorsConf().as();
         colorsConf.setFieldValueColorMap(fieldValueColorMap);
         colorsConf.setLabelFields(labelFields);
         colorsConf.setBackgroundFields(backgroundFields);
         configurationService.put(COLORS_CONF, colorsConf);
         eventBus.fireEvent(new ColorConfUpdatedEvent());
     }
 
     @Override
     public void refresh() {
         if(styleElement != null) {
             styleElement.removeFromParent();
         }
         styleElement = StyleInjector.injectStylesheetAtEnd(generateCssStyleSheet());
     }
 
     @Override
     public boolean isFieldColorizable(final String fieldName)
     {
         return fieldValueColorMap.containsKey(fieldName);
     }
 
     @Override
     public void addColorizableField(final String fieldName)
     {
         if(!fieldValueColorMap.containsKey(fieldName)) {
             fieldValueColorMap.put(fieldName, Maps.<String, Integer>newHashMap());
         }
     }
 
     @Override
     public void removeColorizableField(final String fieldName)
     {
         fieldValueColorMap.remove(fieldName);
         removeLabelField(fieldName);
         removeBackgroundField(fieldName);
     }
 
     @Override
     public Set<String> getColorizableFields()
     {
         return fieldValueColorMap.keySet();
     }
 
     @Override
     public Set<String> getValues(final String fieldName)
     {
         return fieldValueColorMap.get(fieldName).keySet();
     }
 
     @Override
     public String getCssClassName(@Nonnull final String fieldName, @Nonnull final String value)
     {
         assureColorIsMapped(fieldName, value);
         return "hl-"+escape(fieldName)+"-"+escape(value);
     }
 
     private Integer assureColorIsMapped(String fieldName, String value) {
         Map<String, Integer> valueColorMap = fieldValueColorMap.get(fieldName);
         if(valueColorMap == null) {
             valueColorMap = Maps.newHashMap();
             fieldValueColorMap.put(fieldName, valueColorMap);
         }
 
         Integer hue = valueColorMap.get(value);
         if(hue == null) {
             hue = generateColor(fieldName, value);
             valueColorMap.put(value, hue);
         }
         return hue;
     }
 
     private Integer generateColor(String fieldName, String value) {
         String string = fieldName+value;
         return Math.abs(string.hashCode())%360;
     }
 
     private String escape(String str) {
         return str.toLowerCase().replaceAll("\\W+","");
     }
 
     @Override
     public int getColorHue(final String fieldName, final String value)
     {
         return assureColorIsMapped(fieldName, value);
     }
 
     @Override
     public String getCssColor(String fieldName, String value, int saturation, int light) {
         int hue = getColorHue(fieldName, value);
         return getCssColor(hue, saturation, light);
     }
 
     private String getCssColor(int hue, int saturation, int light) {
         return "hsl("+hue+","+saturation+"%,"+light+"%)";
     }
 
     @Override
     public void setColorHue(final String fieldName, final String value, final int hue)
     {
         assureColorIsMapped(fieldName, value);
         fieldValueColorMap.get(fieldName).put(value, hue);
     }
 
     @Override
     public Set<String> getLabelFields()
     {
         return labelFields;
     }
 
     @Override
     public void addLabelField(final String fieldName)
     {
         addColorizableField(fieldName);
         labelFields.add(fieldName);
     }
 
     @Override
     public void removeLabelField(final String fieldName)
     {
         labelFields.remove(fieldName);
     }
 
     @Override
     public void clearLabelFields()
     {
         labelFields.clear();
     }
 
     @Override
     public Set<String> getBackgroundFields()
     {
         return backgroundFields;
     }
 
     @Override
     public void addBackgroundField(final String fieldName)
     {
         addColorizableField(fieldName);
         backgroundFields.add(fieldName);
     }
 
     @Override
     public void removeBackgroundField(final String fieldName)
     {
         backgroundFields.remove(fieldName);
     }
 
     @Override
     public void clearBackgroundFields()
     {
         backgroundFields.clear();
     }
 }
