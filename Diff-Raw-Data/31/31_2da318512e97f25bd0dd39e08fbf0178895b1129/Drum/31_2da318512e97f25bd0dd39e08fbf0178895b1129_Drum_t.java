 package analyticgeometry;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import processing.core.PApplet;
 import processing.core.PVector;
 
 public class Drum implements KinectInstrument {
 	List<DrumSingle> _myDrums = new ArrayList<>();
 
 	private float _myNumbrOfDrums; // Anzahl Saiten
 	private float _myDrumSpace; // Abstand Saiten
 	private float _myFredDistance; // Abstand COM - Tail
 
 	public PApplet p;
 	public boolean debug = true;
 	private Midi midi;
 
 	private boolean fredValue = false;
 
 	public Drum(float _myNumbrOfDrums, float _myDrumSpace, float _myFredDistance, PApplet p, Midi midi) {
 		super();
 		this._myNumbrOfDrums = _myNumbrOfDrums;
 		this._myDrumSpace = _myDrumSpace;
 		this._myFredDistance = _myFredDistance;
 
 		this.p = p;
 		this.midi = midi;
 
 		generateDrums(_myNumbrOfDrums);
 	}
 
 	private void generateDrums(float _myNumbrOfDrums) {
 		_myDrums.clear();
 		if (_myNumbrOfDrums < 1)
 			return;
 		float padding = -(_myNumbrOfDrums - 1) / 2;
 		for (int i = 0; i < _myNumbrOfDrums; i++) {
 			_myDrums.add(new DrumSingle(padding, i));
 			padding += 1;
 
 			// System.out.println(padding);
 		}
 	}
 
 	public void update(Player player) {
 		PVector v3 = player.neck.get();
 
 		// Richtungsvektor zu neck
 		PVector rv = new PVector(v3.x - player.centerOfMass.x, v3.y - player.centerOfMass.y);
 		rv.normalize();
 
 		// Ortsvektor -> Orthogonal zu RV
 		PVector ov = new PVector(rv.y, -rv.x);
 		ov.normalize();
 
 		// Position des Necks
 		PVector neckPos = ov.get();
 		neckPos.mult(_myFredDistance / 2);
 
 		// Position des Freds
 		PVector fredPos = ov.get();
 		fredPos.mult(-_myFredDistance / 2);
 
 		for (DrumSingle myString : _myDrums) {
 			// Start und Ende verschieben
 			myString.start().set(neckPos);
 			myString.end().set(fredPos);
 
 			myString.centerOfVector = myString.end().get();
 		}
 	}
 
 	public void checkFredMatch(Player player) {
 		PVector v2 = player.getHandRightAbsolute().get();
 		v2.normalize();
 
 		for (DrumSingle myString : _myDrums) {
 			// Vektor von Center of Vector zu Ende/Start
 			PVector rv2 = new PVector(myString.start().x - myString.centerOfVector.x, myString.start().y
 					- myString.centerOfVector.y);
 			rv2.normalize();
 
 			// Orthogonaler Vektor zum RV2
 			PVector ov2 = new PVector(rv2.y, -rv2.x);
 			ov2.normalize();
 			// ov2.mult(60f);
 
 			float dotProduct = v2.dot(ov2);
			int neckValue = 0;
 			// Crap
 			if (myString.dotProduct < 0 && dotProduct > 0) {
 				// System.out.println("pass");
				fredValue = true;
				System.out.println("hit up - Neck"+ neckValue);
				midi.playMidi(myString.id, neckValue, true);
 
 			} else if (myString.dotProduct > 0 && dotProduct < 0) {
				fredValue = true;
				midi.playMidi(myString.id, neckValue, false);
				System.out.println("hit down- Neck"+ neckValue);
 			}

			// Neues Dot Product Speichern
			myString.dotProduct = dotProduct;

			//System.out.println(dotProduct);
 		}
 
 	}
 
 	@Override
 	public int checkNeckMatch(Player player) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void checkHeadFred() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void draw(Player player) {
 		p.stroke(255, 0, 255);
 		p.strokeWeight(2);
 
 		for (DrumSingle myString : _myDrums) {
 			p.stroke(0, 255, 255);
 			// p.line(myString.start().x, myString.start().y, myString.end().x,
 			// myString.end().y);
 			p.line(myString.start().x, myString.start().y, myString.end().x, myString.end().y);
 			p.rect(myString.start().x, myString.start().y, _myFredDistance, _myFredDistance);
 		}
 
 		// Draw Player
 		// p.ellipse(player.getHandLeftAbsolute().x,player.getHandLeftAbsolute().y,
 		// 10, 10);
 		// p.ellipse(player.getHandRightAbsolute().x,
 		// player.getHandRightAbsolute().y, 10, 10);
 	}
 
 }
