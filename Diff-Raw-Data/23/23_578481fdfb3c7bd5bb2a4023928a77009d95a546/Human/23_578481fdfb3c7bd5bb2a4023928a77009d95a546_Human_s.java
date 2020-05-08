 package players;
 
 import game.Field;
 import java.util.Scanner;
 
 /**
  * Игрок-человек
  */
 public class Human extends Player {
 
     public Human(String name) {
         super(name);
     }
 
     @Override
     public int go(Field field) {
         //после каждого запроса на ввод координат открываем снова поток ввода,
         // чтобы не считать случайно старые какие-либо значения.
         Scanner scanner = new Scanner(System.in);
         while (true) {
             System.out.print("Введите координаты клетки: ");
             int x = getInt(scanner) - 1;
             int y = getInt(scanner) - 1;
             int valid = field.validationStep(x, y);
             if (valid == Field.NO_ERROR) {
                 return field.width * x + y;
             } else if (valid == Field.ERROR_OUT_OF_BOUNDS) {
                 System.out.println("Ошибочные координаты!");
             } else if (valid == Field.ERROR_USED_CELL) {
                 System.out.println("Клетка занята!");
             } else {
                 System.out.println("Неизвестная ошибка!");
             }
         }
     }
 
     /**
      * Возвращает целое число из потока scanner
      *
      * @param scanner
      * @return
      */
     private int getInt(Scanner scanner) {

         while (!scanner.hasNextInt()) {
             System.out.println("Ошибка! Нужно целое число.");
             scanner.next();
         }
         return scanner.nextInt();
     }
 }
