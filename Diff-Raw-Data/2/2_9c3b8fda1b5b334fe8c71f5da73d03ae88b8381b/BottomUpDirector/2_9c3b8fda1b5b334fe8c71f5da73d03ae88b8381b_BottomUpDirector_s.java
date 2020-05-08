 import java.util.Vector;
 
 
 public class BottomUpDirector extends Director {
 
 	@Override
 	public Operation construct(Vector<Expression> array) {
 		for(int i = array.size() - 1; i > 1; i = i - 2) {
			if(array.get(i / 2) instanceof Operation)
 				getBuilder().build((Operation)array.get((i - 1) / 2), array.get(i - 1), array.get(i));
 		}
 		return (Operation)array.get(0);
 	}
 }
