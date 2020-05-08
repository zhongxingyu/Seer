 package com.github.colorlines.consoleplayer;
 
 import com.github.colorlines.domain.*;
 
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * User: Alex Lenkevich
  * Date: 27.11.11
  * Time: 12:52
  */
 class PlayerImpl implements Player {
 
     private final ColorToStringConverter colorToStringConverter = new ColorToStringConverter();
     private final static Pattern movePattern = Pattern.compile("(\\d)\\s*(\\d)\\s*(\\d)\\s*(\\d)");
 
     public Turn turn(Area area, TurnValidator validator) {
         StringBuffer buf = new StringBuffer();
         buf.append("| ");
         for (int x = Position.WIDTH_RANGE.lowerEndpoint(); x <= Position.WIDTH_RANGE.upperEndpoint(); x++) {
             buf.append("|").append(x);
         }
         buf.append("|\n");
         for (int y = Position.HEIGHT_RANGE.lowerEndpoint(); y <= Position.HEIGHT_RANGE.upperEndpoint(); y++) {
             buf.append("|").append(y);
             for (int x = Position.WIDTH_RANGE.lowerEndpoint(); x <= Position.WIDTH_RANGE.upperEndpoint(); x++) {
                 Position position = Position.create(x, y);
                 buf.append("|").append(
                         area.contains(position)
                                 ? colorToStringConverter.convert(area.take(position).color())
                                 : " "
                 );
             }
             buf.append("|\n");
         }
         System.out.println(buf);
         while (true) {
            System.out.println("Your turn (X Y X Y) :");
             Scanner scanner = new Scanner(System.in);
             String moveText = scanner.nextLine();
             Matcher matcher = movePattern.matcher(moveText);
             if (matcher.matches()) {
                 int xs = Integer.parseInt(matcher.group(1));
                 int ys = Integer.parseInt(matcher.group(2));
                 int xd = Integer.parseInt(matcher.group(3));
                 int yd = Integer.parseInt(matcher.group(4));
 
                 Position startPos = Position.create(xs, ys);
                 Position endPos = Position.create(xd, yd);
 
                 if (area.contains(startPos)) {
                     Ball ball = area.take(startPos);
                     Turn turn = new TurnImpl(ball, endPos);
                     if (validator.isValid(area, turn)) {
                         return turn;
                     }
                 }
             }
             System.out.println("Not corrected move");
         }
     }
 }
