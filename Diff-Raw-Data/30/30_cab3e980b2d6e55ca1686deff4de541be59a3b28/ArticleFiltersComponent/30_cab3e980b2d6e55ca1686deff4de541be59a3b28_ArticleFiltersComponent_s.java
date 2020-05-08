 package com.lavida.swing.form.component;
 
 import com.lavida.service.FilterColumn;
 import com.lavida.service.FilterType;
 import com.lavida.service.FiltersPurpose;
 import com.lavida.service.ViewColumn;
 import com.lavida.service.entity.ArticleJdo;
 import com.lavida.swing.LocaleHolder;
 import com.lavida.swing.service.ArticlesTableModel;
 import org.springframework.context.MessageSource;
 
 import javax.swing.*;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.table.TableRowSorter;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.lang.reflect.Field;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.List;
 import java.util.Queue;
 import java.util.regex.Pattern;
 
 /**
  * ArticleFiltersComponent
  * Created: 20:09 16.08.13
  *
  * @author Pavel
  */
 public class ArticleFiltersComponent {
     private static final List<String> FORBIDDEN_ROLES = new ArrayList<String>();
 
     static {
         FORBIDDEN_ROLES.add("ROLE_SELLER_LA_VIDA");
         FORBIDDEN_ROLES.add("ROLE_SELLER_SLAVYANKA");
         FORBIDDEN_ROLES.add("ROLE_SELLER_NOVOMOSKOVSK");
     }
 
 
     private ArticlesTableModel tableModel;
     private MessageSource messageSource;
     private LocaleHolder localeHolder;
     private List<FilterUnit> filters;
     //    private Map<Integer, >
     private JPanel filtersPanel;
     private JButton clearSearchButton;
     private JCheckBox currentDateCheckBox;
     private TableRowSorter<ArticlesTableModel> sorter;
     private ArticleAnalyzeComponent articleAnalyzeComponent = new ArticleAnalyzeComponent();
 
     public void initializeComponents(ArticlesTableModel tableModel, MessageSource messageSource, LocaleHolder localeHolder) {
         this.tableModel = tableModel;
         this.messageSource = messageSource;
         this.localeHolder = localeHolder;
         this.filters = new ArrayList<FilterUnit>();
         FiltersPurpose filtersPurpose = tableModel.getFiltersPurpose();
 
         FilterElementsListener filterElementsListener = new FilterElementsListener();
 
 //      panel for search operations
         filtersPanel = new JPanel();
         filtersPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(messageSource.
                 getMessage("mainForm.panel.search.title", null, localeHolder.getLocale())),
                 BorderFactory.createEmptyBorder()));
         filtersPanel.setLayout(new GridBagLayout());
 
         GridBagConstraints constraints = new GridBagConstraints();
         constraints.fill = GridBagConstraints.HORIZONTAL;
         constraints.insets = new Insets(1, 5, 1, 5);
 
         boolean sellPurpose = FiltersPurpose.SELL_PRODUCTS == filtersPurpose;
         boolean soldPurpose = FiltersPurpose.SOLD_PRODUCTS == filtersPurpose;
         boolean addNewPurpose = FiltersPurpose.ADD_NEW_PRODUCTS == filtersPurpose;
         for (Field field : ArticleJdo.class.getDeclaredFields()) {
             FilterColumn filterColumn = field.getAnnotation(FilterColumn.class);
             if (filterColumn != null) {
                 if (sellPurpose && filterColumn.showForSell() || soldPurpose && filterColumn.showForSold() ||
                         addNewPurpose && filterColumn.showForAddNew()) {
                     FilterUnit filterUnit = new FilterUnit();
                     if (sellPurpose) {
                         filterUnit.order = filterColumn.orderForSell();
                     } else if (soldPurpose) {
                         filterUnit.order = filterColumn.orderForSold();
                     } else if (addNewPurpose) {
                         filterUnit.order = filterColumn.orderForAddNew();
                     }
 
 //                    filterUnit.order = sellPurpose ? filterColumn.orderForSell() : filterColumn.orderForSold();
                     filterUnit.order = filterUnit.order == 0 ? Integer.MAX_VALUE : filterUnit.order;
                     filterUnit.filterType = filterColumn.type();
                     filterUnit.columnTitle = getColumnTitle(field, messageSource, localeHolder);
                     filterUnit.columnDatePattern = getColumnDatePattern(field);
 
                     if (!filterColumn.labelKey().isEmpty()) {
                         filterUnit.label = new JLabel(messageSource.getMessage(filterColumn.labelKey(), null, localeHolder.getLocale()));
                         filterUnit.textField = new JTextField(filterColumn.editSize());
                         filterUnit.textField.getDocument().addDocumentListener(filterElementsListener);
                     }
 
                     if (filterColumn.checkBoxesNumber() > 0) {
                         filterUnit.checkBoxes = new JCheckBox[filterColumn.checkBoxesNumber()];
                         for (int i = 0; i < filterColumn.checkBoxesNumber(); ++i) {
                             String text = messageSource.getMessage(filterColumn.checkBoxesText()[i], null, localeHolder.getLocale());
                             String actionCommand = messageSource.getMessage(filterColumn.checkBoxesAction()[i], null, localeHolder.getLocale());
                             filterUnit.checkBoxes[i] = new JCheckBox(text);
                             filterUnit.checkBoxes[i].setActionCommand(actionCommand);
                             filterUnit.checkBoxes[i].addItemListener(new ItemListener() {
                                 @Override
                                 public void itemStateChanged(ItemEvent e) {
                                     int state = e.getStateChange();
                                     if (state == ItemEvent.SELECTED) {
                                         applyFilters();
                                         updateAnalyzeComponent();
 
                                     } else if (state == ItemEvent.DESELECTED) {
                                         applyFilters();
                                         updateAnalyzeComponent();
                                     }
 
                                 }
                             });
                         }
                     }
                     filters.add(filterUnit);
                 }
             }
         }
         Collections.sort(filters, new Comparator<FilterUnit>() {
             @Override
             public int compare(FilterUnit filterUnit1, FilterUnit filterUnit2) {
                 return filterUnit1.order - filterUnit2.order;
             }
         });
         for (int i = 0; i < filters.size(); ++i) {
             if (filters.get(i).label != null) {
                 filters.get(i).label.setLabelFor(filters.get(i).textField);
                 constraints.fill = GridBagConstraints.NONE;
                 constraints.gridwidth = GridBagConstraints.RELATIVE;
                 constraints.anchor = GridBagConstraints.EAST;
                 constraints.weightx = 0.0;
                 filtersPanel.add(filters.get(i).label, constraints);
                 constraints.fill = GridBagConstraints.HORIZONTAL;
                 constraints.gridwidth = GridBagConstraints.REMAINDER;
                 constraints.anchor = GridBagConstraints.EAST;
                 constraints.weightx = 1.0;
                 filtersPanel.add(filters.get(i).textField, constraints);
             } else if (filters.get(i).checkBoxes != null) {
                 JPanel checkBoxPanel = new JPanel();
                 checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.LINE_AXIS));
                 checkBoxPanel.add(Box.createHorizontalGlue());
                 for (int j = 0; j < filters.get(i).checkBoxes.length; ++j) {
                     checkBoxPanel.add(filters.get(i).checkBoxes[j]);
                     checkBoxPanel.add(Box.createHorizontalGlue());
                 }
                 constraints.fill = GridBagConstraints.HORIZONTAL;
                 constraints.gridwidth = GridBagConstraints.REMAINDER;
                 constraints.anchor = GridBagConstraints.EAST;
                 constraints.weightx = 1.0;
                 filtersPanel.add(checkBoxPanel, constraints);
             }
         }
         if (soldPurpose) {
             currentDateCheckBox = new JCheckBox();
             currentDateCheckBox.setText(messageSource.getMessage("dialog.sold.products.checkBox.current.date.title",
                     null, localeHolder.getLocale()));
             final String saleDateColumn = messageSource.getMessage("mainForm.table.articles.column.sell.date.title", null, localeHolder.getLocale());
             currentDateCheckBox.addItemListener(new ItemListener() {
 
                 @Override
                 public void itemStateChanged(ItemEvent e) {
                     int state = e.getStateChange();
                     if (state == ItemEvent.SELECTED) {
                         String currentDate = new SimpleDateFormat("dd.MM.yyyy").format(Calendar.getInstance().getTime());
                         for (FilterUnit filterUnit : filters) {
                             if (saleDateColumn.equalsIgnoreCase(filterUnit.columnTitle)) {
                                 filterUnit.textField.setText(currentDate);
                             }
                         }
                     } else if (state == ItemEvent.DESELECTED) {
                         for (FilterUnit filterUnit : filters) {
                             if (saleDateColumn.equalsIgnoreCase(filterUnit.columnTitle)) {
                                 filterUnit.textField.setText("");
                             }
                         }
                     }
                 }
             });
             constraints.gridx = 0;
             constraints.gridy = filters.size() + 1;
             constraints.gridwidth = GridBagConstraints.RELATIVE;
             constraints.anchor = GridBagConstraints.EAST;
             constraints.weightx = 0.0;
             filtersPanel.add(currentDateCheckBox, constraints);
         }
 
         clearSearchButton = new JButton(messageSource.getMessage("mainForm.button.clear.title", null,
                 localeHolder.getLocale()));
         clearSearchButton.setPreferredSize(new Dimension(500, 20));
         clearSearchButton.setMaximumSize(new Dimension(500, 20));
         clearSearchButton.setMinimumSize(new Dimension(500, 20));
         constraints.gridx = 1;
         constraints.gridy = filters.size() + 1;
         constraints.gridwidth = GridBagConstraints.REMAINDER;
         constraints.anchor = GridBagConstraints.EAST;
         constraints.weightx = 1.0;
         clearSearchButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 for (FilterUnit filterUnit : filters) {
                     if (filterUnit.label != null) {
                         filterUnit.textField.setText("");
                     } else if (filterUnit.checkBoxes != null) {
                         for (int j = 0; j < filterUnit.checkBoxes.length; ++j) {
                            filterUnit.checkBoxes[j].setSelected(false);
                         }
 
                     }
                 }
             }
         });
         filtersPanel.add(clearSearchButton, constraints);
         sorter = new TableRowSorter<ArticlesTableModel>(tableModel);
 
         articleAnalyzeComponent.initializeComponents(tableModel, messageSource, localeHolder);
 
     }
 
     private String getColumnTitle(Field field, MessageSource messageSource, LocaleHolder localeHolder) {
         ViewColumn viewColumn = field.getAnnotation(ViewColumn.class);
         return viewColumn != null ? messageSource.getMessage(viewColumn.titleKey(), null, localeHolder.getLocale()) : field.getName();
     }
 
     private String getColumnDatePattern(Field field) {
         ViewColumn viewColumn = field.getAnnotation(ViewColumn.class);
         return viewColumn != null ? viewColumn.datePattern() : null;
     }
 
     /**
      * Filters table by name, by code, by price.
      */
     private void applyFilters() {
         List<RowFilter<ArticlesTableModel, Integer>> andFilters = new ArrayList<RowFilter<ArticlesTableModel, Integer>>();
        for (FilterUnit filterUnit : filters) {
             final int columnIndex = tableModel.findColumn(filterUnit.columnTitle);
 
             RowFilter<ArticlesTableModel, Integer> filter = null;
             if (FilterType.PART_TEXT == filterUnit.filterType) {
                 if (filterUnit.textField.getText().length() > 0) {
                     filter = RowFilter.regexFilter(("(?iu)" + filterUnit.textField.getText().trim()), columnIndex);
                 }
             } else if (FilterType.FULL_TEXT == filterUnit.filterType) {
                 if (filterUnit.textField.getText().length() > 0) {
                     filter = RowFilter.regexFilter(filterUnit.textField.getText().trim(), columnIndex);
                 }
             } else if (FilterType.NUMBER == filterUnit.filterType
                     || FilterType.NUMBER_DIAPASON == filterUnit.filterType && !filterUnit.textField.getText().contains("-")) {
                 if (filterUnit.textField.getText().length() > 0) {
                     String numberStr = filterUnit.textField.getText().trim().replace(",", ".").replaceAll("[^0-9.]", "");
                     Double number = Double.parseDouble(numberStr);
                     filter = RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, number, columnIndex);
                 }
             } else if (FilterType.NUMBER_DIAPASON == filterUnit.filterType) {
                 if (filterUnit.textField.getText().length() > 0) {
                     String[] numbers = filterUnit.textField.getText().split("-", 2);
                     if (numbers.length > 1 && !numbers[0].trim().isEmpty() && !numbers[1].trim().isEmpty()) {
                         String numbers0 = numbers[0].replace(",", ".").replaceAll("[^0-9.]", "");
                         String numbers1 = numbers[1].replace(",", ".").replaceAll("[^0-9.]", "");
                         final Double number1 = Double.parseDouble(numbers0);
                         final Double number2 = Double.parseDouble(numbers1);
                         filter = new RowFilter<ArticlesTableModel, Integer>() {
                             @Override
                             public boolean include(Entry<? extends ArticlesTableModel, ? extends Integer> entry) {
                                 Double number = (Double) tableModel.getRawValueAt(entry.getIdentifier(), columnIndex);
                                 return number > number1 && number < number2;
                             }
                         };
                     }
                 }
             } else if (FilterType.DATE == filterUnit.filterType
                     || FilterType.DATE_DIAPASON == filterUnit.filterType && !filterUnit.textField.getText().contains("-")) {
                 if (filterUnit.textField.getText().length() > 0) {
                     Date correctedDate = getCorrectedDate(filterUnit.textField.getText().trim());
                     String correctedDateString = new SimpleDateFormat(filterUnit.columnDatePattern).format(correctedDate);
                     filter = RowFilter.regexFilter(correctedDateString, columnIndex);
                 }
             } else if (FilterType.DATE_DIAPASON == filterUnit.filterType) {
                 if (filterUnit.textField.getText().length() > 0) {
                     String[] dates = filterUnit.textField.getText().split("-", 2);
                     if (dates.length > 1 && !dates[0].trim().isEmpty() && !dates[1].trim().isEmpty()) {
                         final Date correctedDate1 = getCorrectedDate(dates[0]);
                         final Date correctedDate2 = getCorrectedDate(dates[1]);
                         filter = new RowFilter<ArticlesTableModel, Integer>() {
                             @Override
                             public boolean include(Entry<? extends ArticlesTableModel, ? extends Integer> entry) {
                                 Object saleDateObj = tableModel.getRawValueAt(entry.getIdentifier(), columnIndex);
                                 if (saleDateObj != null) {
                                     Date date = ((Calendar) tableModel.getRawValueAt(entry.getIdentifier(), columnIndex)).getTime();
                                     return date.after(addDays(correctedDate1, -1)) && date.before(addDays(correctedDate2, 1));
                                 } else {
                                     return false;
                                 }
                             }
                         };
                     }
                 }
             } else if (FilterType.CHECKBOXES == filterUnit.filterType) {
                List<RowFilter<ArticlesTableModel, Integer>> checkBoxFilters = new ArrayList<RowFilter<ArticlesTableModel, Integer>>();
                RowFilter<ArticlesTableModel, Integer> checkBoxFilter;
                for (JCheckBox checkBox : filterUnit.checkBoxes) {
                    if (checkBox.isSelected()) {
                        checkBoxFilter = RowFilter.regexFilter((checkBox.getActionCommand()), columnIndex);
//                        checkBoxFilter = RowFilter.regexFilter(Pattern.quote(checkBox.getActionCommand()), columnIndex);
                        checkBoxFilters.add(checkBoxFilter);
                    }
                }
                if (checkBoxFilters.size() > 0) {
                    filter = RowFilter.andFilter(checkBoxFilters);
                 }
             }
 
             if (filter != null) {
                 andFilters.add(filter);
             }
             sorter.setRowFilter(RowFilter.andFilter(andFilters));
         }
     }
 
     private Date addDays(Date date, int daysCount) {
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(date);
         calendar.add(Calendar.DATE, daysCount);
         return calendar.getTime();
     }
 
     /**
      * Returns date in the format: neededDatePattern, in case if year or month isn't entered, current year/month is put.
      *
      * @return date in the format: neededDatePattern.
      */
     private Date getCorrectedDate(String enteredDate) throws NoSuchElementException {
         enteredDate = enteredDate.replaceAll("[^0-9.,]", "");
         Queue<String> dateParts = new ArrayDeque<String>(3);
         StringBuilder number = new StringBuilder();
         for (char symbol : enteredDate.toCharArray()) {
             if (Character.isDigit(symbol)) {
                 number.append(symbol);
             } else if (number.length() > 0) {
                 dateParts.add(number.toString());
                 number = new StringBuilder();
             }
         }
         if (number.length() > 0) {
             dateParts.add(number.toString());
         }
 
         Calendar currentDate = Calendar.getInstance();
         switch (dateParts.size()) {
             case 0:
                 dateParts.add(Integer.toString(currentDate.get(Calendar.DATE)));
             case 1:
                 dateParts.add(Integer.toString(currentDate.get(Calendar.MONTH) + 1));
             case 2:
                 dateParts.add(Integer.toString(currentDate.get(Calendar.YEAR)));
         }
 
         try {
             return new SimpleDateFormat("dd.MM.yyyy").parse(dateParts.remove() + '.' + dateParts.remove() + '.' + dateParts.remove());
 
         } catch (ParseException e) {
             throw new RuntimeException(e);  // todo change exception
         }
     }
 
     /**
      * Updates fields of the articleAnalyzeComponent.
      */
     public void updateAnalyzeComponent() {
         int totalCount = 0;
         double totalCostEUR = 0;
         double totalPriceUAH = 0;
         double totalPurchaseCostEUR = 0;
         double totalCostUAH = 0;
         double normalMultiplierSum = 0;
         double normalMultiplier = 0;
         double minimalMultiplier = 0;
         int viewRows = sorter.getViewRowCount();
         List<ArticleJdo> selectedArticles = new ArrayList<ArticleJdo>();
         for (int i = 0; i < viewRows; i++) {
             int row = sorter.convertRowIndexToModel(i);
             selectedArticles.add(tableModel.getArticleJdoByRowIndex(row));
         }
 
         if (selectedArticles.size() > 0) {
             minimalMultiplier = selectedArticles.get(0).getMultiplier();
             for (ArticleJdo articleJdo : selectedArticles) {
                 ++totalCount;
                 totalPurchaseCostEUR += articleJdo.getPurchasePriceEUR();
                 totalCostEUR += articleJdo.getTotalCostEUR();
                 totalCostUAH += articleJdo.getTotalCostUAH();
                 totalPriceUAH += (articleJdo.getSalePrice());
                 if (minimalMultiplier > articleJdo.getMultiplier()) {
                     minimalMultiplier = articleJdo.getMultiplier();
                 }
                 normalMultiplierSum += articleJdo.getMultiplier();
             }
             normalMultiplier = normalMultiplierSum / totalCount;
         }
 
         articleAnalyzeComponent.updateFields(totalCount, totalPurchaseCostEUR, totalCostEUR, totalCostUAH,
                 minimalMultiplier, normalMultiplier, totalPriceUAH);
     }
 
     class FilterElementsListener implements DocumentListener {
         @Override
         public void insertUpdate(DocumentEvent e) {
             applyFilters();
             updateAnalyzeComponent();
         }
 
         @Override
         public void removeUpdate(DocumentEvent e) {
             applyFilters();
             updateAnalyzeComponent();
         }
 
         @Override
         public void changedUpdate(DocumentEvent e) {
             applyFilters();
             updateAnalyzeComponent();
         }
     }
 
     /**
      * Removes filters from the filterPanel according to users' roles.
      *
      * @param userRoles the list of user's role.
      */
     public void removeFiltersByRoles(List<String> userRoles) {
         if (hasForbiddenRole(userRoles)) {
             Component[] components = filtersPanel.getComponents();
             for (Component component : components) {
                 if (component instanceof JLabel) {
                     JLabel label = (JLabel) component;
                     if (label.getText().equals(messageSource.getMessage("mainForm.label.search.by.shop",
                             null, localeHolder.getLocale()))) {
                         JTextField textField = (JTextField) label.getLabelFor();
                         textField.setEnabled(false);
                         textField.setVisible(false);
                         label.setVisible(false);
                     }
                 }
             }
         }
     }
 
     private boolean hasForbiddenRole(java.util.List<String> userRoles) {
         for (String role : userRoles) {
             if (FORBIDDEN_ROLES.contains(role)) {
                 return true;
             }
         }
         return false;
     }
 
     public JPanel getFiltersPanel() {
         return filtersPanel;
     }
 
     public TableRowSorter<ArticlesTableModel> getSorter() {
         return sorter;
     }
 
     public List<FilterUnit> getFilters() {
         return filters;
     }
 
     public ArticleAnalyzeComponent getArticleAnalyzeComponent() {
         return articleAnalyzeComponent;
     }
 
     public JButton getClearSearchButton() {
         return clearSearchButton;
     }
 }
