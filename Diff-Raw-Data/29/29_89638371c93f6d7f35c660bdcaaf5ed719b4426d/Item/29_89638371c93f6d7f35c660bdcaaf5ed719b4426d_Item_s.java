 package JNeuralNet;
 
 import java.util.Random;
 
 public enum Item {
 	EMPTY 		(0),
 	WALL 		(1),
 	LANDMINE 	(2),
 	ROBOT		(4);
 	
 	int flags;
 	
 	Item(int f){
 		this.flags = f;
 	}
 	Item setFlag(int f) {
 		flags |= f;
 		return this;
 	}
 	Item setFlag(Item i) {
 		setFlag(i.flags);
 		return this;
 	}
 	void removeFlag(int f) {
 		flags &= ~f;
 	}
 	void removeFlag(Item i) {
 		removeFlag(i.flags);
 	}
 	boolean isFlagSet(int f) {
 		return ((flags & f)!=0)?true:false;
 	}
 	boolean isFlagSet(Item i) {
 		return isFlagSet(i.flags);
 	}
 	int getFlags(){
 		return flags;
 	}
 	Item randItem() {
 		if(flags==0) return Item.EMPTY;
 		Random r = new Random();
 		int randomFlag = (int)Math.pow(2,r.nextInt((int)Math.floor(Math.log(flags)/Math.log(2))));
 		while(!isFlagSet(randomFlag)) {
 			randomFlag = (int)Math.pow(2,r.nextInt((int)Math.floor(Math.log(flags)/Math.log(2))));
 		}
 		if(Math.floor(Math.log(randomFlag)/Math.log(2)) != Math.log(randomFlag)/Math.log(2))
 		{
 			System.err.println("ARGH! randItem() is broken!");
 			System.exit(-1);
 		}
 		return Item.EMPTY.setFlag(randomFlag);
 	}
 }
