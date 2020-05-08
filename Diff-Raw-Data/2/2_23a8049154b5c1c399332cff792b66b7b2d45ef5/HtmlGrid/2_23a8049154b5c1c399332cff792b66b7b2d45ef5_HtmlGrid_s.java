 package com.psddev.dari.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class HtmlGrid {
 
     private final List<CssUnit> columns;
     private final List<CssUnit> rows;
     private final List<List<String>> template;
 
     public HtmlGrid(String columnsString, String rowsString, String... templateStrings) {
         columns = createCssUnits(columnsString);
         rows = createCssUnits(rowsString);
         template = new ArrayList<List<String>>();
 
         for (String t : templateStrings) {
             if (t != null && t.length() > 0) {
                 for (String line : t.split("[\\r\\n]+")) {
                     line = line.trim();
 
                     if (line.length() > 0) {
                         List<String> words = Arrays.asList(line.split("\\s+"));
                         int wordsSize = words.size();
 
                         if (wordsSize > 0) {
                             int lastIndex = wordsSize - 1;
                             String lastWord = words.get(lastIndex);
 
                             if (lastWord.startsWith("/")) {
                                 rows.add(new CssUnit(lastWord.substring(1)));
                                 words.remove(lastIndex);
                             }
                         }
 
                         wordsSize = words.size();
                         int columnsSize = columns.size();
 
                         if (wordsSize != columnsSize) {
                             throw new IllegalArgumentException(String.format(
                                     "Columns mismatch! [%s] items in [%s] but [%s] in [%s]",
                                     wordsSize, line, columnsSize, columnsString));
                         }
 
                         template.add(words);
                     }
                 }
             }
         }
 
         int templateSize = template.size();
         int rowsSize = rows.size();
 
         if (templateSize != rowsSize) {
             StringBuilder t = new StringBuilder();
 
             if (templateStrings != null) {
                 for (String templateString : templateStrings) {
                     t.append("\n");
                     t.append(templateString);
                 }
             }
 
             throw new IllegalArgumentException(String.format(
                     "Rows mismatch! [%s] items in [%s] but [%s] in [%s]",
                     templateSize, t, rowsSize, rowsString));
         }
     }
 
     private List<CssUnit> createCssUnits(String values) {
         List<CssUnit> instances = new ArrayList<CssUnit>();
 
         if (values != null) {
             for (String value : values.trim().split("\\s+")) {
                 instances.add(new CssUnit(value));
             }
         }
 
         return instances;
     }
 
     public List<CssUnit> getColumns() {
         return columns;
     }
 
     public List<CssUnit> getRows() {
         return rows;
     }
 
     public List<List<String>> getTemplate() {
         return template;
     }
 
     private CssCombinedUnit combineNonFractionals(List<CssUnit> units) {
         List<CssUnit> filtered = new ArrayList<CssUnit>();
 
         for (CssUnit unit : units) {
             if (!"fr".equals(unit.getUnit())) {
                 filtered.add(unit);
             }
         }
 
         return new CssCombinedUnit(filtered);
     }
 
     public CssCombinedUnit getMinimumWidth() {
         return combineNonFractionals(getColumns());
     }
 
     public CssCombinedUnit getMinimumHeight() {
         return combineNonFractionals(getRows());
     }
 
     /** Returns all CSS units used by this template. */
     public Set<String> getCssUnits() {
         Set<String> units = new HashSet<String>();
 
         for (CssUnit column : getColumns()) {
             units.add(column.getUnit());
         }
 
         for (CssUnit row : getRows()) {
             units.add(row.getUnit());
         }
 
         return units;
     }
 
     /** Returns all area names used by this template. */
     public Set<String> getAreas() {
         Set<String> areas = new LinkedHashSet<String>();
 
         for (List<String> row : getTemplate()) {
             for (String area : row) {
                 if (!".".equals(area)) {
                     areas.add(area);
                 }
             }
         }
 
         return areas;
     }
 
     public static final class Static {
 
         private static final Logger LOGGER = LoggerFactory.getLogger(HtmlGrid.class);
 
         private static final String ATTRIBUTE_PREFIX = HtmlGrid.class.getName() + ".";
         private static final String CSS_MODIFIED_ATTRIBUTE_PREFIX = ATTRIBUTE_PREFIX + "cssModified.";
         private static final String GRID_PATHS_ATTRIBUTE = ATTRIBUTE_PREFIX + "gridPaths";
         private static final String GRIDS_ATTRIBUTE_PREFIX = ATTRIBUTE_PREFIX + "grids.";
 
         private static final String TEMPLATE_PROPERTY = "grid-template";
         private static final String COLUMNS_PROPERTY = "grid-definition-columns";
         private static final String ROWS_PROPERTY = "grid-definition-rows";
 
         public static Map<String, HtmlGrid> findAll(ServletContext context) throws IOException {
             return findGrids(context, findGridPaths(context));
         }
 
         public static void addStyleSheet(HttpServletRequest request, String path) {
             @SuppressWarnings("unchecked")
             List<String> paths = (List<String>) request.getAttribute(GRID_PATHS_ATTRIBUTE);
 
             if (paths == null) {
                 paths = new ArrayList<String>();
                 request.setAttribute(GRID_PATHS_ATTRIBUTE, paths);
             }
 
            paths.add(path);
         }
 
         public static Map<String, HtmlGrid> findAll(ServletContext context, HttpServletRequest request) throws IOException {
             @SuppressWarnings("unchecked")
             List<String> usedPaths = request != null ? (List<String>) request.getAttribute(GRID_PATHS_ATTRIBUTE) : null;
             List<String> gridPaths = findGridPaths(context);
 
             return findGrids(context, usedPaths == null || usedPaths.isEmpty() ? gridPaths : usedPaths);
         }
 
         /** @deprecated Use {@link #findAll} instead. */
         @Deprecated
         public static HtmlGrid find(ServletContext context, String cssClass) throws IOException {
             return ObjectUtils.isBlank(cssClass) ? null : findAll(context).get("." + cssClass);
         }
 
         @SuppressWarnings("unchecked")
         private static List<String> findGridPaths(ServletContext context) throws IOException {
             List<String> gridPaths = null;
 
             if (Settings.isProduction()) {
                 gridPaths = (List<String>) context.getAttribute(GRID_PATHS_ATTRIBUTE);
             }
 
             if (gridPaths == null) {
                 gridPaths = new ArrayList<String>();
 
                 findGridPathsNamed(context, "/", gridPaths, ".less");
                 findGridPathsNamed(context, "/", gridPaths, ".css");
                 context.setAttribute(GRID_PATHS_ATTRIBUTE, gridPaths);
             }
 
             return gridPaths;
         }
 
         private static void findGridPathsNamed(
                 ServletContext context,
                 String path,
                 List<String> gridPaths,
                 String suffix)
                 throws IOException {
 
             Set<String> children = CodeUtils.getResourcePaths(context, path);
 
             if (children == null) {
                 return;
             }
 
             for (String child : children) {
                 if (child.endsWith("/")) {
                     findGridPathsNamed(context, child, gridPaths, suffix);
 
                 } else if (child.endsWith(suffix)) {
                     String modifiedAttr = CSS_MODIFIED_ATTRIBUTE_PREFIX + child;
                     URLConnection cssConnection = CodeUtils.getResource(context, child).openConnection();
                     InputStream cssInput = cssConnection.getInputStream();
 
                     gridPaths.add(child);
 
                     try {
                         Long oldModified = (Long) context.getAttribute(modifiedAttr);
                         long cssModified = cssConnection.getLastModified();
 
                         if (oldModified != null && oldModified == cssModified) {
                             continue;
                         }
 
                         LOGGER.info("Reading stylesheet [{}] modified [{}]", child, cssModified);
 
                         Css css = new Css(IoUtils.toString(cssInput, StringUtils.UTF_8));
                         Map<String, HtmlGrid> grids = new LinkedHashMap<String, HtmlGrid>();
 
                         for (CssRule rule : css.getRules()) {
                             if (!"grid".equals(rule.getValue("display"))) {
                                 continue;
                             }
 
                             String selector = rule.getSelector();
                             LOGGER.info("Found grid matching [{}] in [{}]", selector, child);
 
                             String templateValue = rule.getValue(TEMPLATE_PROPERTY);
 
                             if (ObjectUtils.isBlank(templateValue)) {
                                 throw new IllegalStateException(String.format(
                                         "Path: [%s], Selector: [%s], Missing [%s]!",
                                         child, selector, TEMPLATE_PROPERTY));
                             }
 
                             String columnsValue = rule.getValue(COLUMNS_PROPERTY);
 
                             if (ObjectUtils.isBlank(columnsValue)) {
                                 throw new IllegalStateException(String.format(
                                         "Path: [%s], Selector: [%s], Missing [%s]!",
                                         child, selector, COLUMNS_PROPERTY));
                             }
 
                             String rowsValue = rule.getValue(ROWS_PROPERTY);
 
                             if (ObjectUtils.isBlank(rowsValue)) {
                                 throw new IllegalStateException(String.format(
                                         "Path: [%s], Selector: [%s], Missing [%s]!",
                                         child, selector, ROWS_PROPERTY));
                             }
 
                             char[] letters = templateValue.toCharArray();
                             StringBuilder word = new StringBuilder();
                             List<String> list = new ArrayList<String>();
 
                             for (int i = 0, length = letters.length; i < length; ++ i) {
                                 char letter = letters[i];
 
                                 if (letter == '"') {
                                     for (++ i; i < length; ++ i) {
                                         letter = letters[i];
 
                                         if (letter == '"') {
                                             list.add(word.toString());
                                             word.setLength(0);
                                             break;
 
                                         } else {
                                             word.append(letter);
                                         }
                                     }
 
                                 } else if (Character.isWhitespace(letter)) {
                                     if (word.length() > 0) {
                                         list.add(word.toString());
                                         word.setLength(0);
                                     }
 
                                 } else {
                                     word.append(letter);
                                 }
                             }
 
                             StringBuilder t = new StringBuilder();
 
                             for (String v : list) {
                                 t.append(v);
                                 t.append("\n");
                             }
 
                             try {
                                 grids.put(selector, new HtmlGrid(
                                         columnsValue,
                                         rowsValue,
                                         t.toString()));
 
                             } catch (IllegalArgumentException error) {
                                 throw new IllegalArgumentException(String.format(
                                         "Path: [%s], Selector: [%s], %s",
                                         child, selector, error.getMessage()));
                             }
                         }
 
                         context.setAttribute(modifiedAttr, cssModified);
                         context.setAttribute(GRIDS_ATTRIBUTE_PREFIX + child, grids);
 
                     } finally {
                         cssInput.close();
                     }
                 }
             }
         }
 
         private static Map<String, HtmlGrid> findGrids(ServletContext context, List<String> gridPaths) {
             Map<String, HtmlGrid> all = new LinkedHashMap<String, HtmlGrid>();
 
             for (int i = gridPaths.size() - 1; i >= 0; -- i) {
                 String gridPath = gridPaths.get(i);
                 @SuppressWarnings("unchecked")
                 Map<String, HtmlGrid> grids = (Map<String, HtmlGrid>) context.getAttribute(GRIDS_ATTRIBUTE_PREFIX + gridPath);
 
                 if (grids != null) {
                     all.putAll(grids);
                 }
             }
 
             return all;
         }
     }
 }
