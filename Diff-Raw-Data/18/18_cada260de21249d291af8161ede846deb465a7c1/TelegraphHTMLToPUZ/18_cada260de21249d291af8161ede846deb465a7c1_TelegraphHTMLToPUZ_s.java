 package org.richardneish.crosswords.converter;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;      
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.*;
 import org.jsoup.select.*;
 import com.epeterso2.jabberwordy.model.PuzzleModel;
 import com.epeterso2.jabberwordy.model.PuzzleModelClue;
 import com.epeterso2.jabberwordy.modelconversion.*;
 import com.epeterso2.jabberwordy.serialization.puz.*;
 import com.epeterso2.jabberwordy.util.*;
 
 public class TelegraphHTMLToPUZ {
   private static final Logger log = Logger.getLogger(TelegraphHTMLToPUZ.class.getName());
 
   public static PUZPuzzle createPUZPuzzle(Document doc) throws PuzzleModelConversionException {
     try {
       // Get the HTML elements containing the grid.
       Elements rows = doc.select("html>body>table>tbody>tr>td>center>table>tbody tr");
       int height = rows.size();
       int width = rows.get(0).select("td").size();
       log.info("Found puzzle grid with " + width + " columns and " + height + " rows.");
 
       // Create the blank puzzle object.
       PUZPuzzle puzzle = new PUZPuzzle(width, height);
 
       // Construct the grid.
       CoordinateMap<PUZCellStyle> cellStyles = puzzle.getCellStyles();
       for (int y=0; y<height; y++) {
         Elements cellImages = rows.get(y).select("td > img");
         for (int x=0; x<width; x++) {
           String cellImage = cellImages.get(x).attr("src");
           PUZCellStyle cellStyle = cellStyles.get(x+1, y+1);
           cellStyle.setBlock(cellImage != null && cellImage.endsWith("black_cell.gif"));
         }
       }
       puzzle.assignClueNumbers();
 
       // Add the clues.
       List<Clue> clueList = new ArrayList<Clue>();
       for (int i = 0; i<2; i++) {
         Elements clues = doc.select("html body table tbody tr td font b").get(i).parent().parent().select("clue");
         for (Element clue : clues) {
           int clueNumber = Integer.parseInt(clue.parent().parent().parent().select("b").first().text());
           String clueText = clue.text();
           Clue.Direction direction = i==0 ? Clue.Direction.ACROSS : Clue.Direction.DOWN;
           log.info("Clue number " + clueNumber + " " + direction.name() + " is '" + clueText + "'.");
           clueList.add(new Clue(direction, clueNumber, clueText));
         }
       }
       Collections.sort(clueList);
       List<String> clueStrings = new ArrayList<String>(clueList.size());
       for (Clue clue : clueList) {
         log.fine("Sorted Clue number " + clue.number + " " + clue.direction.name() + " is '" + clue.text + "'.");
         clueStrings.add(clue.text);
       }
       puzzle.assignClues(clueStrings);
 
      // Add default solution to get around the check in Jabberwordy library.
      // TODO:Fix Jabberwordy to allow puzzle without solutions.
      CoordinateMap<PUZSolution> solutions = puzzle.getSolutions();
      for (Map.Entry<Coordinate,PUZSolution> entry : solutions.entrySet()) {
        entry.getValue().setLetter('X');
      }

       // Add the puzzle title.
       String title = doc.select(".telegraph").first().text();
       title = title.replaceAll("\u00a0+", " ").trim();
       log.info("Puzzle title is '" + title + "'.");
       puzzle.setTitle(title);
 
       return puzzle;
     } catch (Exception e) {
       throw new PuzzleModelConversionException(e);
     }
   }
 
   public static PUZPuzzle addSolutions(PUZPuzzle puzzle, Document doc) throws PuzzleModelConversionException {
     // Extract the solution text and clue number and assign to the grid.
     List<Clue> solutionList = new ArrayList<Clue>();
     for (int i = 0; i<2; i++) {
       // Get the solution elements for this direction (across or down).
       Elements solutions = doc.select("html body table tbody tr td font b").get(i).parent().parent().select("solution");
       Map<Integer, String> clues = i==0 ? puzzle.getAcrossClues() : puzzle.getDownClues();
       for (Element solution : solutions) {
         int solutionNumber = Integer.parseInt(solution.parent().parent().parent().select("b").first().text());
         String solutionText = solution.text();
         Clue.Direction direction = i==0 ? Clue.Direction.ACROSS : Clue.Direction.DOWN;
         log.fine("Solution number " + solutionNumber + " " + direction.name() + " is '" + solutionText + "'.");
         // Store the solution.
         solutionList.add(new Clue(direction, solutionNumber, solutionText));
       }
     }
     Collections.sort(solutionList);
 
     // Iterate through the grid, looking for clue numbers.
     Map<Integer, Coordinate> clueNumberMap = new HashMap<Integer, Coordinate>();
     CoordinateMap<PUZCellStyle> cellStyles = puzzle.getCellStyles();
     for (int x=1; x<=puzzle.getWidth(); x++) {
       for (int y=1; y<=puzzle.getHeight(); y++) {
         PUZCellStyle cellStyle = cellStyles.get(x, y);
         Integer clueNumber = Integer.valueOf(cellStyle.getNumber());
         clueNumberMap.put(clueNumber, new Coordinate(x, y));
       }
     }
 
     // Populate the solutions
     CoordinateMap<PUZSolution> solutions = puzzle.getSolutions();
     for (Clue solution : solutionList) {
       if (!clueNumberMap.containsKey(solution.number)) {
         throw new PuzzleModelConversionException("Cannot find coordinates for solution " + solution.number);
       }
       Coordinate cellCoords = clueNumberMap.get(Integer.valueOf(solution.number)).clone();
       log.fine("Writing solution '" + solution + "' starting at location " + cellCoords + ".");
       for (int pos=0; pos<solution.text.length(); pos++) {
         if (cellStyles.get(cellCoords).isBlock()) {
           throw new PuzzleModelConversionException("Attempted to write solution '" +
               solution + "', position " + pos + " at location " + cellCoords +
               ", but found a blocked cell.");
         }
         char solutionChar = solution.text.charAt(pos);
         char foundLetter = solutions.get(cellCoords).getLetter();
         if (foundLetter != '\u0000' && foundLetter != solutionChar) {
           throw new PuzzleModelConversionException("Solution mismatch.  Expected '" + solutionChar +
               "' at location " + cellCoords + " but found '" +
               foundLetter + "'.");
         } else {
           log.fine("Putting letter '" + solutionChar + " at position " + cellCoords + ".");
           solutions.put(cellCoords.getX(), cellCoords.getY(), new PUZSolution(solutionChar));
         }
         switch (solution.direction) {
           case ACROSS:
             cellCoords.setX(cellCoords.getX()+1);
             break;
           case DOWN:
             cellCoords.setY(cellCoords.getY()+1);
             break;
         }
       }
     }
 
     return puzzle;
   }
 
   public static final void main(String[] args) {
     TelegraphHTMLToPUZ c = new TelegraphHTMLToPUZ();
     try {
       // Create the puzzle.
       PUZPuzzle puzzle = c.createPUZPuzzle(Jsoup.parse(new File(args[0]),null));
 
       // Add the solutions.
       try {
         addSolutions(puzzle, Jsoup.parse(new File(args[1]),null));
       } catch (Exception e) {
         log.warning("Exception adding solution: " + e.getMessage());
        log.warning("Continuing without solution.");
       }
 
       // Write out the PUZ file.
       PUZPuzzleInputStream is = new PUZPuzzleInputStream(puzzle);
       FileOutputStream os = new FileOutputStream(args[2]);
       os.write(is.toByteArray());
 
     } catch (Exception e) {
       throw new RuntimeException(e);
     }
   }
 }
 
 class Clue implements Comparable<Clue> {
   enum Direction {
     ACROSS,
     DOWN
   }
 
   public final Direction direction;
   public final int number;
   public final String text;
 
   public Clue(Direction direction, int number, String text) {
     this.direction = direction;
     this.number = number;
     this.text = text;
   }
 
   @Override
     public int compareTo(Clue other) {
       if (other == null) {
         return 1;
       }
       int numberCompare = this.number - other.number;
       if (numberCompare != 0) {
         return numberCompare;
       } else if (this.direction == Direction.ACROSS && other.direction == Direction.DOWN) {
         return -1;
       } else if (this.direction == Direction.DOWN && other.direction == Direction.ACROSS) {
         return 1;
       } else {
         return 0;
       }
     }
 
   @Override
     public String toString() {
       return direction.name() + " " + number + ": " + text;
     }
 }
