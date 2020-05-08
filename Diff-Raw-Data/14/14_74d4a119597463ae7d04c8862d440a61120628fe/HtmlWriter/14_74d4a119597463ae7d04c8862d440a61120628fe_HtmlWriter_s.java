 package com.psddev.dari.util;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.lang.reflect.Array;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.UUID;
 
 /** Writer implementation that adds basic HTML formatting. */
 public class HtmlWriter extends Writer {
 
     private final Writer writer;
     private final Map<Class<?>, HtmlFormatter<Object>> defaultFormatters = new HashMap<Class<?>, HtmlFormatter<Object>>();
     private final Map<Class<?>, HtmlFormatter<Object>> overrideFormatters = new HashMap<Class<?>, HtmlFormatter<Object>>();
     private final Deque<String> tags = new ArrayDeque<String>();
 
     /** Creates an instance that writes to the given {@code writer}. */
     public HtmlWriter(Writer writer) {
         this.writer = writer;
     }
 
     @SuppressWarnings("unchecked")
     public <T> void putDefault(Class<T> objectClass, HtmlFormatter<? super T> formatter) {
         defaultFormatters.put(objectClass, (HtmlFormatter<Object>) formatter);
     }
 
     @SuppressWarnings("unchecked")
     public <T> void putOverride(Class<T> objectClass, HtmlFormatter<? super T> formatter) {
         overrideFormatters.put(objectClass, (HtmlFormatter<Object>) formatter);
     }
 
     public void putAllStandardDefaults() {
         putDefault(null, HtmlFormatter.NULL);
         putDefault(Class.class, HtmlFormatter.CLASS);
         putDefault(Collection.class, HtmlFormatter.COLLECTION);
         putDefault(Date.class, HtmlFormatter.DATE);
         putDefault(Double.class, HtmlFormatter.FLOATING_POINT);
         putDefault(Enum.class, HtmlFormatter.ENUM);
         putDefault(Float.class, HtmlFormatter.FLOATING_POINT);
         putDefault(Map.class, HtmlFormatter.MAP);
         putDefault(Number.class, HtmlFormatter.NUMBER);
         putDefault(PaginatedResult.class, HtmlFormatter.PAGINATED_RESULT);
         putDefault(StackTraceElement.class, HtmlFormatter.STACK_TRACE_ELEMENT);
         putDefault(Throwable.class, HtmlFormatter.THROWABLE);
 
         // Optional.
         if (HtmlFormatter.JASPER_EXCEPTION_CLASS != null) {
             putDefault(HtmlFormatter.JASPER_EXCEPTION_CLASS, HtmlFormatter.JASPER_EXCEPTION);
         }
     }
 
     public void removeDefault(Class<?> objectClass) {
         defaultFormatters.remove(objectClass);
     }
 
     public void removeOverride(Class<?> objectClass) {
         overrideFormatters.remove(objectClass);
     }
 
     /**
      * Escapes the given {@code string} so that it's safe to use in
      * an HTML page.
      */
     protected String escapeHtml(String string) {
         return StringUtils.escapeHtml(string);
     }
 
     private void writeAttribute(Object name, Object value) throws IOException {
         if (!(ObjectUtils.isBlank(name) || value == null)) {
             writer.write(' ');
             writer.write(escapeHtml(name.toString()));
             writer.write("=\"");
             writer.write(escapeHtml(value.toString()));
             writer.write('"');
         }
     }
 
     /**
      * Writes the given {@code tag} with the given {@code attributes}.
      *
      * <p>This method doesn't keep state, so it should be used with doctype
      * declaration and self-closing tags like {@code img}.</p>
      */
     public HtmlWriter tag(String tag, Object... attributes) throws IOException {
         if (tag == null) {
             throw new IllegalArgumentException("Tag can't be null!");
         }
 
         writer.write('<');
         writer.write(tag);
 
         if (attributes != null) {
             for (int i = 0, length = attributes.length; i < length; ++ i) {
                 Object name = attributes[i];
 
                 if (name instanceof Map) {
                     for (Map.Entry<?, ?> entry : ((Map<?, ?>) name).entrySet()) {
                         writeAttribute(entry.getKey(), entry.getValue());
                     }
 
                 } else {
                     ++ i;
                     Object value = i < length ? attributes[i] : null;
                     writeAttribute(name, value);
                 }
             }
         }
 
         writer.write('>');
         return this;
     }
 
     /**
      * Writes the given start {@code tag} with the given {@code attributes}.
      *
      * <p>This method keeps state, so there should be a matching {@link #end}
      * call afterwards.</p>
      */
     public HtmlWriter start(String tag, Object... attributes) throws IOException {
         tag(tag, attributes);
         tags.addFirst(tag);
         return this;
     }
 
     /** Writes the end tag previously started with {@link #start}. */
     public HtmlWriter end() throws IOException {
         String tag = tags.removeFirst();
 
         if (tag == null) {
             throw new IllegalStateException("No more tags!");
         }
 
         writer.write("</");
         writer.write(tag);
         writer.write('>');
 
         return this;
     }
 
     /**
      * Escapes and writes the given {@code unescapedHtml}, or if it's
      * {@code null}, the given {@code defaultUnescapedHtml}.
      */
     public HtmlWriter htmlOrDefault(Object unescapedHtml, String defaultUnescapedHtml) throws IOException {
         writer.write(escapeHtml(unescapedHtml == null ? defaultUnescapedHtml : unescapedHtml.toString()));
         return this;
     }
 
     /**
      * Escapes and writes the given {@code unescapedHtml}, or if it's
      * {@code null}, nothing.
      */
     public HtmlWriter html(Object unescapedHtml) throws IOException {
         htmlOrDefault(unescapedHtml, "");
         return this;
     }
 
     /** Formats and writes the given {@code object}. */
     public HtmlWriter object(Object object) throws IOException {
         HtmlFormatter<Object> formatter;
 
         if (object == null) {
             formatter = overrideFormatters.get(null);
             if (formatter == null) {
                 formatter = defaultFormatters.get(null);
             }
             if (formatter != null) {
                 formatter.format(this, null);
                 return this;
             }
 
         } else {
             if (formatWithMap(overrideFormatters, object)) {
                 return this;
             }
 
             if (object instanceof HtmlObject) {
                 ((HtmlObject) object).format(this);
                 return this;
             }
 
             if (formatWithMap(defaultFormatters, object)) {
                 return this;
             }
         }
 
         if (object != null && object.getClass().isArray()) {
             start("ul");
                 for (int i = 0, length = Array.getLength(object); i < length; ++ i) {
                     start("li").object(Array.get(object, i)).end();
                 }
             end();
             return this;
         }
 
         return html(object);
     }
 
     private boolean formatWithMap(
             Map<Class<?>, HtmlFormatter<Object>> formatters,
             Object object)
             throws IOException {
 
         HtmlFormatter<Object> formatter;
 
         for (Class<?> objectClass = object.getClass();
                 objectClass != null;
                 objectClass = objectClass.getSuperclass()) {
 
             formatter = formatters.get(objectClass);
             if (formatter != null) {
                 formatter.format(this, object);
                 return true;
             }
 
             for (Class<?> interfaceClass : objectClass.getInterfaces()) {
                 formatter = formatters.get(interfaceClass);
                 if (formatter != null) {
                     formatter.format(this, object);
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     /** Returns a CSS string based on the given {@code properties}. */
     public String cssString(Object... properties) {
         return Static.cssString(properties);
     }
 
     /** Writes a CSS rule based on the given parameters. */
     public HtmlWriter css(String selector, Object... properties) throws IOException {
         write(selector);
         write('{');
         write(cssString(properties));
         write("}\n");
         return this;
     }
 
     /**
      * Writes the given {@code object} and positions it according to the
      * given {@code grid}.
      *
      * @see <a href="http://dev.w3.org/csswg/css3-grid-layout/">CSS Grid Layout</a>
      */
     public HtmlWriter grid(Object object, HtmlGrid grid, boolean inlineCss) throws IOException {
         Map<String, Area> areas = createAreas(grid);
         boolean debug;
 
         try {
             debug = !Settings.isProduction() && ObjectUtils.to(boolean.class, PageContextFilter.Static.getRequest().getParameter("_grid"));
         } catch (Exception error) {
             debug = false;
         }
 
         if (!inlineCss) {
             start("style", "type", "text/css");
                 css(".dari-grid-area",
                         "-moz-box-sizing", "content-box",
                         "-webkit-box-sizing", "content-box",
                         "box-sizing", "content-box",
                         "float", "left",
                         "margin", "0 -100% 0 -30000px");
 
                 css(".dari-grid-adj",
                         "float", "left");
 
                 for (Area area : areas.values()) {
                     String selector = area.id != null ? "#" + area.id : ".dari-grid-area[data-grid-area=\"" + area.getName() + "\"]";
 
                     css(selector,
                             "clear", area.clear ? "left" : null,
                             "padding-left", area.frPaddingLeft + "%",
                             "width", area.frWidth + "%");
 
                     for (Map.Entry<String, Adjustment> entry : area.adjustments.entrySet()) {
                         String unit = entry.getKey();
                         Adjustment adjustment = entry.getValue();
 
                        css(selector + " .dari-grid-adj-" + unit,
                                 "height", adjustment.height,
                                 "margin", adjustment.getMargin(unit),
                                 "width", adjustment.width);
                     }
                 }
             end();
         }
 
         if (object == null) {
             object = areas;
         }
 
         for (Map.Entry<String, Area> entry : areas.entrySet()) {
             String areaName = entry.getKey();
             Area area = entry.getValue();
 
             // The main wrapping DIV around the area. Initially shifted
             // left 30000px so that it's off-screen as not to overlap
             // other elements that come before.
             start("div",
                     "class", "dari-grid-area",
                     "id", area.id,
                     "data-grid-area", areaName,
                     "style", !inlineCss ? null : cssString(
                             "-moz-box-sizing", "content-box",
                             "-webkit-box-sizing", "content-box",
                             "box-sizing", "content-box",
                             "clear", area.clear ? "left" : null,
                             "float", "left",
                             "margin", "0 -100% 0 -30000px",
                             "padding-left", area.frPaddingLeft + "%",
                             "width", area.frWidth + "%"));
 
                 int adjustments = 0;
 
                for (Map.Entry<String, Adjustment> entry2 : area.adjustments.entrySet()) {
                     ++ adjustments;
                    String unit = entry2.getKey();
                    Adjustment adjustment = entry2.getValue();
 
                     start("div",
                            "class", "dari-grid-adj dari-grid-adj-" + unit,
                             "style", !inlineCss ? null : cssString(
                                     "float", "left",
                                     "height", adjustment.height,
                                     "margin", adjustment.getMargin(unit),
                                     "width", adjustment.width));
                 }
 
                 if (debug) {
                     start("div", "style", cssString(
                             "border", "3px dashed red",
                             "padding", "3px"));
                 }
 
                 // Minimum width with multiple units.
                 if (area.singleWidth == null) {
                     int i = 0;
 
                     for (CssUnit column : area.width.getAll()) {
                         if (!"fr".equals(column.getUnit())) {
                             ++ i;
                             start("div", "style", cssString(
                                     "padding-left", column,
                                     "height", 0));
                         }
                     }
 
                     for (; i > 0; -- i) {
                         end();
                     }
                 }
 
                 // Minimum height with multiple units.
                 if (area.singleHeight == null) {
                     start("div", "style", cssString(
                             "float", "left",
                             "width", 0));
 
                     int i = 0;
 
                     for (CssUnit row : area.height.getAll()) {
                         ++ i;
                         start("div", "style", cssString(
                                 "padding-top", row,
                                 "width", 0));
                     }
 
                     for (; i > 0; -- i) {
                         end();
                     }
 
                     end();
                 }
 
                 object(CollectionUtils.getByPath(object, entry.getKey()));
 
                 if (area.singleHeight == null) {
                     start("div", "style", cssString("clear", "left"));
                     end();
                 }
 
                 if (debug) {
                     end();
                 }
 
                 for (; adjustments > 0; -- adjustments) {
                     end();
                 }
 
             end();
         }
 
         start("div", "style", cssString("clear", "left"));
         end();
 
         return this;
     }
 
     private Map<String, Area> createAreas(HtmlGrid grid) {
         List<CssUnit> columns = grid.getColumns();
         List<CssUnit> rows = grid.getRows();
         List<List<String>> template = new ArrayList<List<String>>(grid.getTemplate());
 
         // Clone the template so that the original isn't changed.
         for (ListIterator<List<String>> i = template.listIterator(); i.hasNext(); ) {
             i.set(new ArrayList<String>(i.next()));
         }
 
         Map<String, Area> areaInstances = new LinkedHashMap<String, Area>();
         int clearAt = -1;
 
         for (int rowStart = 0, rowSize = rows.size(); rowStart < rowSize; ++ rowStart) {
             List<String> areas = template.get(rowStart);
 
             for (int columnStart = 0, columnSize = columns.size(); columnStart < columnSize; ++ columnStart) {
                 String area = areas.get(columnStart);
 
                 // Already processed or padding.
                 if (area == null || ".".equals(area)) {
                     continue;
                 }
 
                 int rowStop = rowStart + 1;
                 int columnStop = columnStart + 1;
 
                 // Figure out the "width" of the area.
                 for (; columnStop < columnSize; ++ columnStop) {
                     if (!ObjectUtils.equals(areas.get(columnStop), area)) {
                         break;
                     } else {
                         areas.set(columnStop, null);
                     }
                 }
 
                 // Figure out the "height" of the area.
                 for (; rowStop < rowSize; ++ rowStop) {
                     if (columnStart < template.get(rowStop).size() && !ObjectUtils.equals(template.get(rowStop).get(columnStart), area)) {
                         break;
                     } else {
                         for (int i = columnStart; i < columnStop; ++ i) {
                             if (i < template.get(rowStop).size()) {
                                 template.get(rowStop).set(i, null);
                             }
                         }
                     }
                 }
 
                 // Figure out the rough initial position and size using
                 // percentages.
                 Area areaInstance = new Area(area);
                 areaInstances.put(area, areaInstance);
 
                 double frMax = 0;
                 double frBefore = 0;
                 double frAfter = 0;
 
                 for (int i = 0; i < columnSize; ++ i) {
                     CssUnit column = columns.get(i);
 
                     if ("fr".equals(column.getUnit())) {
                         double fr = column.getNumber();
 
                         frMax += fr;
 
                         if (i < columnStart) {
                             frBefore += fr;
 
                         } else if (i >= columnStop) {
                             frAfter += fr;
                         }
                     }
                 }
 
                 if (frMax == 0) {
                     frMax = 1;
                     frAfter = 1;
                 }
 
                 double frBeforeRatio = frBefore / frMax;
                 double frAfterRatio = frAfter / frMax;
 
                 areaInstance.frPaddingLeft = frBeforeRatio * 100.0;
                 areaInstance.frWidth = (frMax - frBefore - frAfter) * 100.0 / frMax;
 
                 // Adjust left and width.
                 for (int i = 0; i < columnSize; ++ i) {
                     CssUnit column = columns.get(i);
                     String columnUnit = column.getUnit();
 
                     if (!"fr".equals(columnUnit)) {
                         double columnNumber = column.getNumber();
                         double left = columnNumber * ((i < columnStart ? 1 : 0) - frBeforeRatio);
                         double right = columnNumber * ((i >= columnStop ? 1 : 0) - frAfterRatio);
 
                         if (left != 0.0 || right != 0.0) {
                             Adjustment adjustment = areaInstance.getOrCreateAdjustment(columnUnit);
                             adjustment.left += left;
                             adjustment.right += right;
                         }
                     }
                 }
 
                 // Adjust top.
                 for (int i = rowSize - 1; i >= 0; -- i) {
                     CssUnit row = rows.get(i);
                     String rowUnit = row.getUnit();
 
                     if (i < rowStart && "auto".equals(rowUnit)) {
                         break;
 
                     } else if (!"fr".equals(rowUnit)) {
                         double top = row.getNumber() * (i < rowStart ? 1 : 0);
 
                         if (top != 0.0) {
                             Adjustment adjustment = areaInstance.getOrCreateAdjustment(rowUnit);
                             adjustment.top += top;
                         }
                     }
                 }
 
                 // Make sure there's always "px" adjustment layer so that
                 // we can shift right 30000px to cancel out the positioning
                 // from the main wrapping DIV.
                 Adjustment pxAdjustment = areaInstance.adjustments.remove("px");
 
                 if (pxAdjustment == null) {
                     pxAdjustment = new Adjustment();
                 }
 
                 pxAdjustment.left += 30000;
                 pxAdjustment.right -= 30000;
 
                 areaInstance.adjustments.put("px", pxAdjustment);
 
                 // Set width explicitly if there's only one unit.
                 CombinedCssUnit width = areaInstance.width = new CombinedCssUnit(columns.subList(columnStart, columnStop));
                 CssUnit singleWidth = areaInstance.singleWidth = width.getSingle();
 
                 if (singleWidth != null) {
                     pxAdjustment.width = singleWidth;
                 }
 
                 // Set height explicitly if there's only one unit.
                 CombinedCssUnit height = areaInstance.height = new CombinedCssUnit(rows.subList(rowStart, rowStop));
                 CssUnit singleHeight = areaInstance.singleHeight = height.getSingle();
 
                 if (singleHeight != null) {
                     pxAdjustment.height = singleHeight;
                 }
 
                 // Clear because of "auto" height?
                 if (clearAt >= 0 && clearAt <= rowStart) {
                     clearAt = -1;
                     areaInstance.clear = true;
                 }
 
                 if (height.hasAuto() && rowStop > clearAt) {
                     clearAt = rowStop;
                 }
             }
         }
 
         return areaInstances;
     }
 
     /**
      * Writes the given {@code object} and positions it according to the
      * given {@code grid}.
      *
      * @see <a href="http://dev.w3.org/csswg/css3-grid-layout/">CSS Grid Layout</a>
      */
     public HtmlWriter grid(Object object, HtmlGrid grid) throws IOException {
         return grid(object, grid, false);
     }
 
     /**
      * Writes the given {@code object} and positions it according to the
      * grid rules as specified by the given parameters.
      *
      * @see #grid(Object, HtmlGrid)
      */
     public HtmlWriter grid(Object object, String columns, String rows, String... template) throws IOException {
         return grid(object, new HtmlGrid(columns, rows, template));
     }
 
     public static class Area {
 
         private final String name;
         protected final String id = "i" + UUID.randomUUID().toString().replaceAll("-", "");
         protected boolean clear;
         protected double frPaddingLeft;
         protected double frWidth;
         protected CombinedCssUnit width;
         protected CssUnit singleWidth;
         protected CombinedCssUnit height;
         protected CssUnit singleHeight;
         protected final Map<String, Adjustment> adjustments = new LinkedHashMap<String, Adjustment>();
 
         public Area(String name) {
             this.name = name;
         }
 
         public String getName() {
             return name;
         }
 
         protected Adjustment getOrCreateAdjustment(String unit) {
             Adjustment adjustment = adjustments.get(unit);
             if (adjustment == null) {
                 adjustment = new Adjustment();
                 adjustments.put(unit, adjustment);
             }
             return adjustment;
         }
     }
 
     private class CombinedCssUnit {
 
         private final Map<String, CssUnit> combined = new HashMap<String, CssUnit>();
 
         public CombinedCssUnit(Iterable<CssUnit> values) {
             for (CssUnit value : values) {
                 String unit = value.getUnit();
                 CssUnit old = combined.get(unit);
 
                 if (old == null) {
                     combined.put(unit, value);
 
                 } else {
                     combined.put(unit, new CssUnit(old.getNumber() + value.getNumber(), unit));
                 }
             }
 
             for (Iterator<Map.Entry<String, CssUnit>> i = combined.entrySet().iterator(); i.hasNext(); ) {
                 CssUnit value = i.next().getValue();
 
                 if (!"auto".equals(value.getUnit()) && value.getNumber() == 0.0) {
                     i.remove();
                 }
             }
         }
 
         public Collection<CssUnit> getAll() {
             return combined.values();
         }
 
         public CssUnit getSingle() {
             if (combined.size() != 1) {
                 return null;
 
             } else {
                 CssUnit value = combined.values().iterator().next();
                 return "fr".equals(value.getUnit()) ? null : value;
             }
         }
 
         public boolean hasAuto() {
             return combined.keySet().contains("auto");
         }
     }
 
     private static class Adjustment {
 
         public double left;
         public double right;
         public double top;
         public CssUnit width;
         public CssUnit height;
 
         public String getMargin(String unit) {
             return new CssUnit(top, unit) + " " +
                     new CssUnit(right, unit) + " 0 " +
                     new CssUnit(left, unit);
         }
     }
 
     // --- Writer support ---
 
     @Override
     public Writer append(char letter) throws IOException {
         writer.write(letter);
         return this;
     }
 
     @Override
     public Writer append(CharSequence text) throws IOException {
         writer.append(text);
         return this;
     }
 
     @Override
     public Writer append(CharSequence text, int start, int end) throws IOException {
         writer.append(text, start, end);
         return this;
     }
 
     @Override
     public void close() throws IOException {
         writer.close();
     }
 
     @Override
     public void flush() throws IOException {
         writer.flush();
     }
 
     @Override
     public void write(char[] buffer) throws IOException {
         writer.write(buffer);
     }
 
     @Override
     public void write(char[] buffer, int offset, int length) throws IOException {
         writer.write(buffer, offset, length);
     }
 
     @Override
     public void write(int letter) throws IOException {
         writer.write(letter);
     }
 
     @Override
     public void write(String text) throws IOException {
         writer.write(text);
     }
 
     @Override
     public void write(String text, int offset, int length) throws IOException {
         writer.write(text, offset, length);
     }
 
     /** {@link HtmlWriter} utility methods. */
     public static final class Static {
 
         /** Returns a CSS string based on the given {@code properties}. */
         public static String cssString(Object... properties) {
             StringBuilder css = new StringBuilder();
 
             if (properties != null) {
                 for (int i = 1, length = properties.length; i < length; i += 2) {
                     Object property = properties[i - 1];
 
                     if (property != null) {
                         Object value = properties[i];
 
                         if (value != null) {
                             css.append(property);
                             css.append(':');
                             css.append(value);
                             css.append(';');
                         }
                     }
                 }
             }
 
             return css.toString();
         }
     }
 
     // --- Deprecated ---
 
     /** @deprecated Use {@link #htmlOrDefault} instead. */
     @Deprecated
     public HtmlWriter stringOrDefault(Object string, String defaultString) throws IOException {
         return htmlOrDefault(string, defaultString);
     }
 
     /** @deprecated Use {@link #html} instead. */
     @Deprecated
     public HtmlWriter string(Object string) throws IOException {
         return html(string);
     }
 }
