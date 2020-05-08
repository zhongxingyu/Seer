 package com.netxforge.netxstudio.models.export;
 
 import java.io.FileOutputStream;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.poi.hssf.usermodel.HSSFFont;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.hssf.util.HSSFColor;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.IndexedColors;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.eclipse.emf.cdo.CDOObject;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.netxforge.netxstudio.data.IDataProvider;
 
 public class MasterDataExporter {
 
 	@SuppressWarnings("unused")
 	private IDataProvider dataProvider;
 	private EPackage[] ePackages;
 	private Object[] exportObjects = null;
 	@SuppressWarnings("unused")
 	private EStructuralFeature[] featureFilter;
 	@SuppressWarnings("unused")
 	private EClass[] classFilter;
 
 	HSSFWorkbook workBook;
 
 	public void process(FileOutputStream fileOut) {
 
 		try {
 			workBook = new HSSFWorkbook();
 			processPackages(ePackages);
 			workBook.write(fileOut);
 		} catch (final Exception e) {
 			throw new IllegalStateException(e);
 		}
 	}
 
 	private void processPackages(EPackage[] ePackages) {
		ImmutableList<EPackage> of = ImmutableList.copyOf(ePackages);
 		for (EPackage ePackage : of) {
 			this.processPackage(ePackage);
 		}
 	}
 
 	private void processPackage(EPackage ePackage) {
 		for (EClassifier eClassifier : ePackage.getEClassifiers()) {
 			processClassifier(eClassifier);
 		}
 	}
 
 	private void processClassifier(EClassifier eClassifier) {
 		if (eClassifier instanceof EClass) {
 			processAttributesClass((EClass) eClassifier);
 			processMultiRefClass((EClass) eClassifier);
 		}
 	}
 
 	private void processAttributesClass(EClass eClass) {
 		Sheet sheet = _generateAttributeWorksheet(eClass);
 		_generateID(sheet);
 		for (EAttribute eAttribute : filterAttributes(eClass)) {
 			processAttribute(eAttribute, sheet);
 
 		}
 		for (EReference eReference : eClass.getEAllReferences()) {
 			if (!eReference.isMany()) {
 				processReference(eReference, sheet);
 			}
 		}
 		// Process the actual values here.
 		List<EObject> data = contextObjectsForClass(eClass);
 		processAttributeData(data, sheet);
 
 	}
 
 	private void processMultiRefClass(EClass eClass) {
 		List<EReference> multiRefs = this.filterMultiRefs(eClass);
 		if (multiRefs.size() > 0) {
 			Sheet sheet = _generateRefsWorksheet(eClass);
 			_generateMultiReferenceSource(eClass, sheet);
 			for (EReference eReference : multiRefs) {
 				processMultiReference(eReference, sheet);
 			}
 
 			// Process the actual values here.
 			List<EObject> data = contextObjectsForClass(eClass);
 			processMultiRefData(data, sheet);
 		}
 	}
 
 	private void processReference(EReference eReference, Sheet sheet) {
 		_generateCell(eReference, sheet);
 
 	}
 
 	private void processAttribute(EAttribute eAttribute, Sheet sheet) {
 		_generateCell(eAttribute, sheet);
 	}
 
 	private void processMultiReference(EReference eReference, Sheet sheet) {
 		_generateMultiRefCell(eReference, sheet);
 
 	}
 
 	private void processAttributeData(List<EObject> data, Sheet sheet) {
 		// We assume the attribute order for each data object.
 		int objectCount = 2;
 		for (EObject dataObject : data) {
 			// Generate the Identifier. 
 			_generateIDCell(dataObject, objectCount, 0, sheet);
 			
 			int attributeCount = 1;
 			for (EAttribute eAttribute : this.filterAttributes(dataObject
 					.eClass())) {
 				Object value = dataObject.eGet(eAttribute);
 				_generateDataCell(value, objectCount, attributeCount, sheet);
 				attributeCount++;
 			}
 
 			for (EReference eReference : dataObject.eClass()
 					.getEAllReferences()) {
 				if (!eReference.isMany()) {
 					Object refObject = dataObject.eGet(eReference);
 					String identifier = "";
 					if (refObject != null) {
 						identifier = this.contextObjectsIdentifier((EObject) refObject);
 						
 						// FIXME Don not use an identifier which is calculated from an attribute. 
 //						EAttribute idAttribute = contextObjectsIdentifier(((EObject) refObject)
 //								.eClass());
 //						// We always expect our id feature to be a String.
 //						if (idAttribute != null) {
 //							value = (String) ((EObject) refObject)
 //									.eGet(idAttribute);
 //							if (value == null || value.isEmpty()) {
 //								value = "?";
 //							}
 //						}
 						
 						
 					}
 					_generateDataCell(identifier, objectCount, attributeCount, sheet);
 					attributeCount++;
 				}
 			}
 			objectCount++;
 		}
 	}
 
 
 	private void processMultiRefData(List<EObject> data, Sheet sheet) {
 		// We assume the attribute order for each data object.
 		int objectCount = 2;
 		for (EObject dataObject : data) {
 			int referenceCount = 1;
 			for (EReference eReference : this.filterMultiRefs(dataObject
 					.eClass())) {
 				Object collection = dataObject.eGet(eReference);
 				int rowCount = objectCount;
 
 				if (collection instanceof List<?>) {
 					for (Object refObject : (List<?>) collection) {
 
 						// /////////////////
 						// 1. Write the identifier Note: This could be a
 						// substitute.
 						// /////////////////
 						
 						
 						
 						
 						// If we return an attribute from another EClass (As we
 						// might not have
 						// an identifier, we should also switch the object.
 //						EAttribute idDataObjectAttribute = contextObjectsIdentifier(dataObject
 //								.eClass());
 //						// We always expect our id feature to be a String.
 //						String dataObjectValue = "";
 //						if (idDataObjectAttribute != null) {
 //							dataObjectValue = (String) dataObject
 //									.eGet(idDataObjectAttribute);
 //
 //							if (dataObjectValue == null
 //									|| dataObjectValue.isEmpty()) {
 //								dataObjectValue = "?";
 //							}
 //						}
 						
 						String identifier = this.contextObjectsIdentifier(dataObject);
 						
 						_generateDataCell(identifier, rowCount, 0,
 								sheet);
 
 						// ///////////////////////
 						// 2. write the reference.
 						// ///////////////////////
 //						EAttribute idAttribute = contextObjectsIdentifier(((EObject) refObject)
 //								.eClass());
 //						// We always expect our id feature to be a String.
 //						String value = "";
 //						if (idAttribute != null) {
 //							value = (String) ((EObject) refObject)
 //									.eGet(idAttribute);
 //							if (value == null || value.isEmpty()) {
 //								value = "?";
 //							}
 //						}
 						String refIdentifier = this.contextObjectsIdentifier((EObject) refObject);
 						_generateDataCell(refIdentifier, rowCount, referenceCount,
 								sheet);
 						rowCount++;
 					}
 				}
 				referenceCount++;
 				objectCount = rowCount;
 			}
 		}
 	}
 
 	// Context setters.
 	public void setDataProvider(IDataProvider dataProvider) {
 		this.dataProvider = dataProvider;
 	}
 
 	public void setPackagesToExport(EPackage[] ePackages) {
 		this.ePackages = ePackages;
 	}
 
 	public void setExportObjects(Object... contextObjects) {
 		this.exportObjects = contextObjects;
 	}
 
 	private List<EObject> contextObjectsForClass(EClass eClass) {
 		
 		// TODO previous based on a fixed set of objects. 
 //		// We not only get the selected objects, but also their containments.
		ImmutableList<Object> of = ImmutableList.copyOf(exportObjects);
 		List<EObject> result = Lists.newArrayList();
 		for (Object o : of) {
 			EObject eo = (EObject) o;
 			if (eo.eClass() == eClass) {
 				result.add(eo);
 			}
 			// Also add all closure contents.
 			TreeIterator<EObject> ti = eo.eAllContents();
 			List<EObject> closure = ImmutableList.copyOf(ti);
 			for (EObject closureObject : closure) {
 				if (closureObject.eClass() == eClass) {
 					result.add(closureObject);
 				}
 			}
 		}
 		return result;
 	}
 
 	
 	private String contextObjectsIdentifier(EObject eo) {
 		if(eo instanceof CDOObject){
 			return ((CDOObject) eo).cdoID().toString();
 		}
 		return null;
 	}
 //	private EAttribute contextObjectsIdentifier(EClass eClass) {
 //
 //		if (eClass == LibraryPackage.Literals.EQUIPMENT) {
 //			return LibraryPackage.Literals.EQUIPMENT__EQUIPMENT_CODE;
 //		}
 //		if (eClass == LibraryPackage.Literals.FUNCTION) {
 //			return LibraryPackage.Literals.COMPONENT__NAME;
 //		}
 //
 //		if (eClass == LibraryPackage.Literals.NODE_TYPE) {
 //			return LibraryPackage.Literals.NODE_TYPE__NAME;
 //		}
 //		if (eClass == LibraryPackage.Literals.TOLERANCE) {
 //			return LibraryPackage.Literals.TOLERANCE__NAME;
 //		}
 //		if (eClass == LibraryPackage.Literals.EXPRESSION) {
 //			return LibraryPackage.Literals.EXPRESSION__NAME;
 //		}
 //
 //		if (eClass == MetricsPackage.Literals.METRIC_SOURCE) {
 //			return MetricsPackage.Literals.METRIC_SOURCE__NAME;
 //		}
 //
 //		// Mapping has no identifier, export the metric_source name.
 ////		if (eClass == MetricsPackage.Literals.MAPPING_XLS) {
 ////			return MetricsPackage.Literals.METRIC_SOURCE__NAME;
 ////		}
 //
 //		return null;
 //	}
 	
 	
 	private void _generateID(Sheet sheet) {
 		// Generate name.
 		{
 			// Style, cell color.
 			CellStyle attributeStyle = workBook.createCellStyle();
 			attributeStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT
 					.getIndex());
 			attributeStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
 
 			// Style, font
 			HSSFFont attributeFont = workBook.createFont();
 			attributeFont.setFontName("Verdana");
 			attributeFont.setColor(HSSFColor.BLUE.index);
 			attributeStyle.setFont(attributeFont);
 
 			// Style, border.
 			attributeStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
 
 			Row row = sheet.getRow(0);
 			if (row == null) {
 				row = sheet.createRow(0);
 			}
 			short lastCellNum = row.getLastCellNum();
 			if (lastCellNum == -1) {
 				lastCellNum = 0;
 			}
 			Cell cell = row.createCell(lastCellNum);
 			cell.setCellValue("ID");
 			cell.setCellStyle(attributeStyle);
 		}
 		// Generate type
 		{
 
 			CellStyle typeStyle = workBook.createCellStyle();
 			typeStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
 
 			Row row = sheet.getRow(1);
 			if (row == null) {
 				row = sheet.createRow(1);
 			}
 			short lastCellNum = row.getLastCellNum();
 			if (lastCellNum == -1) {
 				lastCellNum = 0;
 			}
 			Cell cell = row.createCell(lastCellNum);
 			cell.setCellStyle(typeStyle);
 		}
 	}
 
 
 	// POI SECTION.
 	private Sheet _generateAttributeWorksheet(EClass eClass) {
 		return workBook.createSheet(eClass.getName());
 	}
 
 	private Sheet _generateRefsWorksheet(EClass eClass) {
 		return workBook.createSheet(eClass.getName() + "_refs");
 	}
 
 	private void _generateCell(EAttribute eAttribute, Sheet sheet) {
 
 		// Generate name.
 		{
 			// Style, cell color.
 			CellStyle attributeStyle = workBook.createCellStyle();
 			attributeStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT
 					.getIndex());
 			attributeStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
 
 			// Style, font
 			HSSFFont attributeFont = workBook.createFont();
 			attributeFont.setFontName("Verdana");
 			attributeFont.setColor(HSSFColor.BLUE.index);
 			attributeStyle.setFont(attributeFont);
 
 			// Style, border.
 			attributeStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
 
 			Row row = sheet.getRow(0);
 			if (row == null) {
 				row = sheet.createRow(0);
 			}
 			short lastCellNum = row.getLastCellNum();
 			if (lastCellNum == -1) {
 				lastCellNum = 0;
 			}
 			Cell cell = row.createCell(lastCellNum);
 			cell.setCellValue(StringUtils.capitalize(eAttribute.getName()));
 			cell.setCellStyle(attributeStyle);
 		}
 		// Generate type
 		{
 
 			CellStyle typeStyle = workBook.createCellStyle();
 			typeStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
 
 			Row row = sheet.getRow(1);
 			if (row == null) {
 				row = sheet.createRow(1);
 			}
 			short lastCellNum = row.getLastCellNum();
 			if (lastCellNum == -1) {
 				lastCellNum = 0;
 			}
 			Cell cell = row.createCell(lastCellNum);
 			cell.setCellValue(eAttribute.getEType().getName());
 			cell.setCellStyle(typeStyle);
 		}
 	}
 
 	private void _generateCell(EReference eReference, Sheet sheet) {
 
 		// Generate name.
 		{
 			// Style, cell color.
 			CellStyle referenceStyle = workBook.createCellStyle();
 			referenceStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT
 					.getIndex());
 			referenceStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
 
 			// Style, font
 			HSSFFont referenceFont = workBook.createFont();
 			referenceFont.setFontName("Verdana");
 			referenceFont.setColor(HSSFColor.DARK_RED.index);
 			referenceStyle.setFont(referenceFont);
 
 			// Style, border.
 			referenceStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
 
 			Row row = sheet.getRow(0);
 			if (row == null) {
 				row = sheet.createRow(0);
 			}
 			short lastCellNum = row.getLastCellNum();
 			if (lastCellNum == -1) {
 				lastCellNum = 0;
 			}
 			Cell cell = row.createCell(lastCellNum);
 			cell.setCellValue(StringUtils.capitalize(eReference.getName()));
 			cell.setCellStyle(referenceStyle);
 		}
 		// Generate type
 		{
 
 			CellStyle typeStyle = workBook.createCellStyle();
 			typeStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
 
 			Row row = sheet.getRow(1);
 			if (row == null) {
 				row = sheet.createRow(1);
 			}
 			short lastCellNum = row.getLastCellNum();
 			if (lastCellNum == -1) {
 				lastCellNum = 0;
 			}
 			Cell cell = row.createCell(lastCellNum);
 			cell.setCellValue(eReference.getEType().getName());
 			cell.setCellStyle(typeStyle);
 		}
 	}
 
 	private void _generateMultiReferenceSource(EClass eClass, Sheet sheet) {
 		// Generate name.
 		{
 			// Style, cell color.
 			CellStyle referenceStyle = workBook.createCellStyle();
 			referenceStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT
 					.getIndex());
 			referenceStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
 
 			// Style, font
 			HSSFFont referenceFont = workBook.createFont();
 			referenceFont.setFontName("Verdana");
 			referenceFont.setColor(HSSFColor.DARK_RED.index);
 			referenceStyle.setFont(referenceFont);
 
 			// Style, border.
 			referenceStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
 
 			Row row = sheet.getRow(0);
 			if (row == null) {
 				row = sheet.createRow(0);
 			}
 			short lastCellNum = row.getLastCellNum();
 			if (lastCellNum == -1) {
 				lastCellNum = 0;
 			}
 			Cell cell = row.createCell(lastCellNum);
 			cell.setCellValue(StringUtils.capitalize(eClass.getName()));
 			cell.setCellStyle(referenceStyle);
 		}
 		// Generate type
 		{
 
 			CellStyle typeStyle = workBook.createCellStyle();
 			typeStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
 
 			Row row = sheet.getRow(1);
 			if (row == null) {
 				row = sheet.createRow(1);
 			}
 			short lastCellNum = row.getLastCellNum();
 			if (lastCellNum == -1) {
 				lastCellNum = 0;
 			}
 			Cell cell = row.createCell(lastCellNum);
 			cell.setCellStyle(typeStyle);
 		}
 	}
 
 	private void _generateMultiRefCell(EReference eReference, Sheet sheet) {
 
 		// Generate name.
 		{
 			// Style, cell color.
 			CellStyle referenceStyle = workBook.createCellStyle();
 			referenceStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT
 					.getIndex());
 			referenceStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
 
 			// Style, font
 			HSSFFont referenceFont = workBook.createFont();
 			referenceFont.setFontName("Verdana");
 			referenceFont.setColor(HSSFColor.DARK_RED.index);
 			referenceStyle.setFont(referenceFont);
 
 			// Style, border.
 			referenceStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
 
 			Row row = sheet.getRow(0);
 			if (row == null) {
 				row = sheet.createRow(0);
 			}
 			short lastCellNum = row.getLastCellNum();
 			if (lastCellNum == -1) {
 				lastCellNum = 0;
 			}
 			Cell cell = row.createCell(lastCellNum);
 			cell.setCellValue(StringUtils.capitalize(eReference.getName()));
 			cell.setCellStyle(referenceStyle);
 		}
 		// Generate type
 		{
 
 			CellStyle typeStyle = workBook.createCellStyle();
 			typeStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
 
 			Row row = sheet.getRow(1);
 			if (row == null) {
 				row = sheet.createRow(1);
 			}
 			short lastCellNum = row.getLastCellNum();
 			if (lastCellNum == -1) {
 				lastCellNum = 0;
 			}
 			Cell cell = row.createCell(lastCellNum);
 			cell.setCellValue(eReference.getEType().getName());
 			cell.setCellStyle(typeStyle);
 		}
 	}
 
 	private void _generateIDCell(EObject dataObject, int rowCount, int cellCount,
 			Sheet sheet) {
 		Row row = sheet.getRow(rowCount);
 		if (row == null) {
 			row = sheet.createRow(rowCount);
 		}
 		Cell cell = row.createCell(cellCount);
 		if (dataObject != null && dataObject instanceof CDOObject) {
 			cell.setCellValue(((CDOObject)dataObject).cdoID().toString());
 		}
 	}
 
 	
 	private void _generateDataCell(Object value, int rowCount, int cellCount,
 			Sheet sheet) {
 		Row row = sheet.getRow(rowCount);
 		if (row == null) {
 			row = sheet.createRow(rowCount);
 		}
 		Cell cell = row.createCell(cellCount);
 		if (value != null) {
 			cell.setCellValue(value.toString());
 		}
 
 	}
 
 	public void setFeatureFilter(EStructuralFeature[] featureFilter) {
 		this.featureFilter = featureFilter;
 	}
 
 	public void setClassFilter(EClass[] classFilter) {
 		this.classFilter = classFilter;
 	}
 
 	public List<EReference> filterMultiRefs(EClass eClass) {
 		List<EReference> multiRefs = Lists.newArrayList();
 		for (EReference eReference : eClass.getEAllReferences()) {
 			if (eReference.isMany() && !eReference.isDerived()) {
 				multiRefs.add(eReference);
 			}
 		}
 		return multiRefs;
 	}
 
 	public List<EAttribute> filterAttributes(EClass eClass) {
 		List<EAttribute> nonDerivedAttributes = Lists.newArrayList();
 		for (EAttribute eAttribute : eClass.getEAllAttributes()) {
 			if (!eAttribute.isDerived()) {
 				nonDerivedAttributes.add(eAttribute);
 			}
 		}
 		return nonDerivedAttributes;
 	}
 
 }
