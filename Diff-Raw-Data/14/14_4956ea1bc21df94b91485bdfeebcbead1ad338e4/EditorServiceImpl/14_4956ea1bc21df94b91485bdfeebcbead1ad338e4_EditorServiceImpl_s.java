 package ch.bfh.ti.projekt1.sokoban.core;
 
 import ch.bfh.ti.projekt1.sokoban.controller.FieldController;
 import ch.bfh.ti.projekt1.sokoban.editor.DraggableElementDestination;
 import ch.bfh.ti.projekt1.sokoban.editor.EditorController;
 import ch.bfh.ti.projekt1.sokoban.editor.LevelEditorView;
 import ch.bfh.ti.projekt1.sokoban.model.Board;
 import ch.bfh.ti.projekt1.sokoban.model.Field;
 import ch.bfh.ti.projekt1.sokoban.model.FieldState;
 import ch.bfh.ti.projekt1.sokoban.xml.Column;
 import ch.bfh.ti.projekt1.sokoban.xml.Level;
 import ch.bfh.ti.projekt1.sokoban.xml.Row;
 
 import java.io.File;
 
 /**
  * @author svennyffenegger
  * @since 31/10/13 20:41
  */
 public class EditorServiceImpl implements EditorService {
     private XmlService xmlService = new XmlServiceImpl();
 
     @Override
     public EditorController getNewLevel(int columns, int rows) {
         EditorController controller = new EditorController();
         Board currentLevel = new Board(columns, rows);
 
         controller.setModel(currentLevel);
 
         LevelEditorView editorView = new LevelEditorView(controller, columns, rows);
         controller.setView(editorView);
 
         for (int i = 0; i < rows; i++) {
             for (int j = 0; j < columns; j++) {
                 Field field = new Field(FieldState.EMPTY);
                 currentLevel.setField(j, i, field);
                 FieldController fieldController = new FieldController();
                 DraggableElementDestination elementDestination = new DraggableElementDestination(fieldController);
 
                 fieldController.setView(elementDestination);
                 fieldController.setModel(field);
 
                 editorView.addElement(elementDestination);
             }
         }
         return controller;
     }
 
     @Override
     public void saveLevel(Board board) throws LevelMisconfigurationException{
         xmlService.saveLevel(board);
     }
 
     @Override
     public EditorController getLevel(File file) {
         Level level = xmlService.getLevelFromFile(file);
         EditorController controller = new EditorController();
         int col = xmlService.getMaxColumnCount(level.getRow());
         int row = level.getRow().size();
 
         Board board = new Board(col, row);
 
         LevelEditorView editorView = new LevelEditorView(controller, col, row);
         controller.setView(editorView);
         controller.setModel(board);
 
         for (Row rowType : level.getRow()) {
             for (Column columnType : rowType.getColumn()) {
                 Field field = new Field(FieldState.parseXmlFieldType(columnType.getType()));
 
                 board.setField(columnType.getId(), rowType.getId(), field);
 
                 FieldController fieldController = new FieldController();
                DraggableElementDestination elementDestination = new DraggableElementDestination(fieldController);
 
                 fieldController.setView(elementDestination);
                 fieldController.setModel(field);
 
                 editorView.addElement(elementDestination);
             }
         }
 
         board.setLevelName(level.getName());
 
         return controller;
     }
 
 
 }
