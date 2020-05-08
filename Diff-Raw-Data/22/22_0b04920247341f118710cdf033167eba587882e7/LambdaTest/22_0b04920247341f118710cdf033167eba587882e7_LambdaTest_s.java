 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.List;
 import java.util.ArrayList;
import java.util.Iterable;
import static java.lang.System.out.println;
 
 // juicy bits
 import java.util.functions.*;
 
 public class LambdaTest {
     private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 
     public static void main(final String[] args) throws ParseException {
         final List<Person> people = new ArrayList<Person>();
         people.add(new Person("Tony", 30, 1));
         people.add(new Person("William", 40, 2));
         people.add(new Person("Jordan", 24, 1));
         people.add(new Person("Nick", 34, 2));
         people.add(new Person("James", 28, 0));
 
         // forEach with Lambda
        people.forEach(p -> {println(p.getName())});
 
         // filter
        Predicate<Integer> encumbered = p -> p.getNumChildren() > 0;
        Iterable<People> parents = people.filter(encumbered)
        parents.forEach(p -> {println(p.getName())};
 
         // filter/map/reduce
     }
 }
 
