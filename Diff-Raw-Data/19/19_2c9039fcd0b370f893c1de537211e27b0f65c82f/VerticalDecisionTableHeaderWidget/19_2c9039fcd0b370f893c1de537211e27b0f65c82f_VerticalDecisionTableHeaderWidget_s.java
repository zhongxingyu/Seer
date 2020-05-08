 package org.drools.guvnor.decisiontable.client.widget;
 
 import org.drools.ide.common.client.modeldriven.dt.ActionCol;
 import org.drools.ide.common.client.modeldriven.dt.AttributeCol;
 import org.drools.ide.common.client.modeldriven.dt.ConditionCol;
 import org.drools.ide.common.client.modeldriven.dt.DTColumnConfig;
 import org.drools.ide.common.client.modeldriven.dt.MetadataCol;
 
 import com.google.gwt.dom.client.DivElement;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.EventTarget;
 import com.google.gwt.dom.client.Style.Overflow;
 import com.google.gwt.dom.client.TableCellElement;
 import com.google.gwt.dom.client.TableElement;
 import com.google.gwt.dom.client.TableRowElement;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class VerticalDecisionTableHeaderWidget extends
 	DecisionTableHeaderWidget {
 
     private HeaderWidget widget;
 
     /**
      * Construct a "Header" for the provided DecisionTable
      * 
      * @param decisionTable
      */
     public VerticalDecisionTableHeaderWidget(DecisionTableWidget dtable) {
 	super(dtable);
 
 	// Construct the Widget
 	panel = new ScrollPanel();
 	widget = new HeaderWidget();
 
 	// We don't want scroll bars on the Header
 	panel.getElement().getStyle().setOverflow(Overflow.HIDDEN);
 	panel.add(widget);
 	initWidget(panel);
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.drools.guvnor.decisiontable.client.widget.DecisionTableHeaderWidget
      * #setScrollPosition(int)
      */
     @Override
     public void setScrollPosition(int position) {
 	((ScrollPanel) this.panel).setHorizontalScrollPosition(position);
     }
 
     @Override
     public void redraw() {
 	widget.redraw();
     }
 
     /**
      * This is the actual header widget.
      * 
      * @author manstis
      * 
      */
     private class HeaderWidget extends Widget {
 
 	private TableElement table;
 
 	public HeaderWidget() {
 	    table = Document.get().createTableElement();
 	    table.setCellPadding(0);
 	    table.setCellSpacing(0);
 	    table.setClassName(resource.cellTableStyle().headerTable());
 
 	    table.appendChild(Document.get().createTRElement());
 	    table.appendChild(Document.get().createTRElement());
 	    sinkEvents(Event.getTypeInt("click"));
 	    setElement(table);
 	}
 
 	@Override
 	public void onBrowserEvent(Event event) {
 	    String type = event.getType();
 
 	    EventTarget eventTarget = event.getEventTarget();
 	    if (!Element.is(eventTarget)
 		    || !getElement().isOrHasChild(Element.as(eventTarget))) {
 		return;
 	    }
 
 	    Element target = event.getEventTarget().cast();
 
 	    // Find the cell where the event occurred.
 	    TableCellElement cell = findNearestParentCell(target);
 	    if (cell == null) {
 		return;
 	    }
 
 	    if (type.equals("click")) {
 		int row = cell.getPropertyInt("row");
 		int col = cell.getPropertyInt("col");
 		DynamicEditColumn column = dtable.getColumns().get(col);
 		switch (row) {
 		case 0:
 		    if (column.getModelColumn() instanceof ConditionCol) {
 			// mergableHeaderClicked(column);
 		    } else {
 			sortableHeaderClicked(column);
 		    }
 		case 1:
 		    if (column.getModelColumn() instanceof ConditionCol) {
 			sortableHeaderClicked(column);
 		    }
 		}
 	    }
 	}
 
 	private TableCellElement findNearestParentCell(Element elem) {
 	    while ((elem != null) && (elem != table)) {
 		String tagName = elem.getTagName();
 		if ("td".equalsIgnoreCase(tagName)
 			|| "th".equalsIgnoreCase(tagName)) {
 		    return elem.cast();
 		}
 		elem = elem.getParentElement();
 	    }
 	    return null;
 	}
 
 	private TableRowElement makeTableRowElement() {
 	    TableRowElement trow = Document.get().createTRElement();
 	    trow.setClassName(resource.cellTableStyle().headerRow());
 	    return trow;
 	}
 
 	public void redraw() {
 	    TableRowElement oldRow;
 	    TableRowElement newRow;
 	    oldRow = table.getRows().getItem(0);
 	    newRow = makeTopRowElement();
 	    table.replaceChild(newRow, oldRow);
 	    oldRow = table.getRows().getItem(1);
 	    newRow = makeBottomRowElement();
 	    table.replaceChild(newRow, oldRow);
 	}
 
 	protected TableRowElement makeTopRowElement() {
 	    TableRowElement trow = makeTableRowElement();
 	    TableCellElement cell = null;
 	    for (DynamicEditColumn column : dtable.columns) {
 		DTColumnConfig modelColumn = column.getModelColumn();
 		if (modelColumn instanceof MetadataCol) {
 		    cell = makeMetadataTableCellElement(column);
 		    cell.setPropertyInt("row", 0);
 		    cell.setPropertyInt("col", column.getColumnIndex());
 		    trow.appendChild(cell);
 		} else if (modelColumn instanceof AttributeCol) {
 		    cell = makeAttributeTableCellElement(column);
 		    cell.setPropertyInt("row", 0);
 		    cell.setPropertyInt("col", column.getColumnIndex());
 		    trow.appendChild(cell);
 		} else if (modelColumn instanceof ConditionCol) {
 		    cell = makeConditionFactTypeTableCellElement(column);
 		    cell.setPropertyInt("row", 0);
 		    cell.setPropertyInt("col", column.getColumnIndex());
 		    trow.appendChild(cell);
 		} else if (modelColumn instanceof ActionCol) {
 		    cell = makeActionTableCellElement(column);
 		    cell.setPropertyInt("row", 0);
 		    cell.setPropertyInt("col", column.getColumnIndex());
 		    trow.appendChild(cell);
 		}
 	    }
 	    return trow;
 	}
 
 	protected TableRowElement makeBottomRowElement() {
 	    TableRowElement trow = makeTableRowElement();
 	    TableCellElement cell = null;
 	    for (DynamicEditColumn column : dtable.columns) {
 		DTColumnConfig modelColumn = column.getModelColumn();
 		if (modelColumn instanceof ConditionCol) {
 		    cell = makeConditionFactFieldTableCellElement(column);
 		    cell.setPropertyInt("row", 1);
 		    cell.setPropertyInt("col", column.getColumnIndex());
 		    trow.appendChild(cell);
 		}
 	    }
 	    return trow;
 	}
 
 	private TableCellElement makeTableCellSortableElement(
 		DynamicEditColumn column) {
 	    TableCellElement tcell = Document.get().createTHElement();
 	    tcell.setClassName(resource.cellTableStyle().headerCellPrimary());
 
 	    DivElement div1 = Document.get().createDivElement();
 	    DivElement div2 = Document.get().createDivElement();
 	    DivElement div3 = Document.get().createDivElement();
 	    div1.setClassName(resource.cellTableStyle().headerContainer());
 	    div2.setClassName(resource.cellTableStyle().headerText());
 	    div3.setClassName(resource.cellTableStyle().headerWidget());
 
 	    div1.appendChild(div2);
 	    div1.appendChild(div3);
 	    div3.setInnerHTML(getSortDirectionIcon(column.getSortDirection(),
 		    column.getSortIndex()));
 
 	    tcell.appendChild(div1);
 	    return tcell;
 
 	}
 
 	private TableCellElement makeTableCellFactTypeElement(
 		DynamicEditColumn column) {
 	    TableCellElement tcell = Document.get().createTHElement();
 	    tcell.setClassName(resource.cellTableStyle().headerCellSecondary());
 
 	    DivElement div1 = Document.get().createDivElement();
 	    DivElement div2 = Document.get().createDivElement();
 	    DivElement div3 = Document.get().createDivElement();
 	    div1.setClassName(resource.cellTableStyle().headerContainer());
 	    div2.setClassName(resource.cellTableStyle().headerText());
 	    div3.setClassName(resource.cellTableStyle().headerWidget());
 
 	    div1.appendChild(div2);
 	    div1.appendChild(div3);
 
 	    tcell.appendChild(div1);
 	    return tcell;
 
 	}
 
 	private TableCellElement makeMetadataTableCellElement(
 		DynamicEditColumn column) {
 	    TableCellElement tcell = makeTableCellSortableElement(column);
 	    setText(tcell, ((MetadataCol) column.getModelColumn()).attr);
 	    tcell.setRowSpan(2);
 	    return tcell;
 	}
 
 	private TableCellElement makeAttributeTableCellElement(
 		DynamicEditColumn column) {
 	    TableCellElement tcell = makeTableCellSortableElement(column);
 	    setText(tcell, ((AttributeCol) column.getModelColumn()).attr);
 	    tcell.setRowSpan(2);
 	    return tcell;
 	}
 
 	private TableCellElement makeConditionFactTypeTableCellElement(
 		DynamicEditColumn column) {
 	    String factType = ((ConditionCol) column.getModelColumn())
 		    .getFactType();
 	    TableCellElement tcell = makeTableCellFactTypeElement(column);
 	    setText(tcell, factType);
 
 	    // Peek ahead to the next column and add "merge" icon if applicable
 	    int iCol = dtable.getColumns().indexOf(column);
 	    if (iCol < dtable.columns.size() - 1) {
 		DTColumnConfig peek = dtable.getColumns().get(iCol + 1)
 			.getModelColumn();
 		if (peek instanceof ConditionCol) {
 		    if (((ConditionCol) peek).getFactType().equals(factType)) {
 			DivElement widget = tcell.getFirstChild().getChild(1)
 				.<DivElement> cast();
 			widget.setInnerHTML(MERGE_LINK);
 		    }
 		}
 	    }
 
 	    return tcell;
 	}
 
 	private TableCellElement makeConditionFactFieldTableCellElement(
 		DynamicEditColumn column) {
 	    TableCellElement tcell = makeTableCellSortableElement(column);
 	    setText(tcell,
 		    ((ConditionCol) column.getModelColumn()).getFactField());
 	    return tcell;
 	}
 
 	private TableCellElement makeActionTableCellElement(
 		DynamicEditColumn column) {
 	    TableCellElement tcell = makeTableCellSortableElement(column);
 	    setText(tcell, ((ActionCol) column.getModelColumn()).getHeader());
 	    tcell.setRowSpan(2);
 	    return tcell;
 	}
 
 	private void setText(TableCellElement cell, String text) {
 	    cell.getFirstChild().getFirstChild().<DivElement> cast()
 		    .setInnerText(text);
 	}
 
 	private String getSortDirectionIcon(SortDirection sd, int sortIndex) {
 	    String html = "";
 	    switch (sd) {
 	    case ASCENDING:
 		html = (sortIndex == 0 ? UP_ARROW : SMALL_UP_ARROW);
 		break;
 	    case DESCENDING:
 		html = (sortIndex == 0 ? DOWN_ARROW : SMALL_DOWN_ARROW);
 	    }
 	    return html;
 	}
 
     }
 
     public void sortableHeaderClicked(DynamicEditColumn header) {
 	updateSortOrder(header);
 	redraw();
 	dtable.manager.sort();
     }
 
     private void updateSortOrder(DynamicEditColumn header) {
 	if (header.getSortIndex() == 0) {
 	    if (header.getSortDirection() != SortDirection.ASCENDING) {
 		header.setSortDirection(SortDirection.ASCENDING);
 	    } else {
 		header.setSortDirection(SortDirection.DESCENDING);
 	    }
 	} else {
 	    header.setSortIndex(0);
 	    header.setSortDirection(SortDirection.ASCENDING);
 	    int sortIndex = 1;
 	    for (DynamicEditColumn sortableHeader : dtable.getColumns()) {
 		if (!sortableHeader.equals(header)) {
 		    if (sortableHeader.getSortDirection() != SortDirection.NONE) {
 			sortableHeader.setSortIndex(sortIndex);
 			sortIndex++;
 		    }
 		}
 	    }
 	}
     }
 
     public void mergableHeaderClicked(DynamicEditColumn column) {
 	Window.alert("clicked");
     }
 
 }
