 package hhu.propra_2013.gruppe_13;
 
 public class Wall extends GameGraphics {
 
 	int HP = 0;
 	double PosX = 0;
 	double PosY = 0;
 	double Rad = 0;
	int tempint = 0;
	double tempdouble = 0;
 	
 	
 	int getHP() {
 		return HP;
 	}
 
 	double getPosX() {
 		return PosX;
 	}
 
 	double getPosY() {
 		return PosY;
 	}
 
 	double getRad() {
 		return Rad;
 	}
 
	void setPosX(tempdouble) {
 		// TODO Auto-generated method stub
 		PosX = tempdouble;
 		return;
 	}
 
 	@Override
	void setPosY(tempdouble) {
 		// TODO Auto-generated method stub
 		PosY = tempdouble;
 		return;
 	}
 
 	@Override
	void setRad(tempdouble) {
 		// TODO Auto-generated method stub
 		Rad = tempdouble;
 		return;
 	}
 
 	@Override
	void setHP(tempint) {
 		// TODO Auto-generated method stub
 		HP = tempint;
 		return;
 	}
 
 }
