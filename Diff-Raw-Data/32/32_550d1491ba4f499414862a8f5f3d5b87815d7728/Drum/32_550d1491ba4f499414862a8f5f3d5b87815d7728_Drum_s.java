 package analyticgeometry;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import processing.core.PApplet;
 import processing.core.PVector;
 
 public class Drum implements KinectInstrument {
 	List<DrumSingle> _myDrums = new ArrayList<>();
 
 	private float _myNumberOfDrums; // Anzahl Drums
 	private float _myDrumSpace; // Abstand Drums
 	private float _myDrumWidth; // Abstand COM - Tail
 	private float _myDrumHeight; // Abstand COM - Tail
 
 	public PApplet p;
 	public boolean debug = true;
 	private Midi midi;
 
 	public Drum(float _myNumberOfDrums, float _myDrumSpace, float _myDrumWidth, float myDrumHeight,
 			PApplet p, Midi midi) {
 		super();
 		this._myNumberOfDrums = _myNumberOfDrums;
 		this._myDrumSpace = _myDrumSpace + _myDrumWidth;
 		this._myDrumWidth = _myDrumWidth;
 		this._myDrumHeight = myDrumHeight;
 
 		this.p = p;
 		this.midi = midi;
 
 		// Drums erstellen
 		generateDrums(_myNumberOfDrums);
 	}
 
 	private void generateDrums(float _myNumbrOfDrums) {
 		_myDrums.clear();
 		if (_myNumbrOfDrums < 1)
 			return;
 		float padding = -(_myNumbrOfDrums - 1) / 2;
 		for (int i = 0; i < _myNumbrOfDrums; i++) {
 			_myDrums.add(new DrumSingle(padding, i));
 			padding += 1;
 		}
 	}
 
 	public void update(Player player) {
 		PVector v3 = player.neck.get();
 
 		// Richtungsvektor zu Neck
 		PVector rv = new PVector(v3.x - player.centerOfMass.x, v3.y - player.centerOfMass.y);
 		rv.normalize();
 
 		// Ortsvektor -> Orthogonal zu RV
 		PVector ov = new PVector(rv.y, -rv.x);
 		ov.normalize();
 
 		// Position des Necks
 		PVector neckPos = ov.get();
 		neckPos.mult(_myDrumWidth / 2);
 
 		// Position des Freds
 		PVector fredPos = ov.get();
 		fredPos.mult(-_myDrumWidth / 2);
 
 		for (DrumSingle myDrum : _myDrums) {
 
 			// Verschiebungsvektor vom COM
 			PVector translation = new PVector(ov.x, ov.y);
 			translation.mult(myDrum.padding * _myDrumSpace);
 
 			// Verschiebungsvektor 2 vom COM
 			PVector translation2 = new PVector(ov.x, ov.y);
 			translation2.mult((myDrum.padding * _myDrumSpace) - _myDrumWidth / 2);
 
 			// Start und Ende verschieben
 			myDrum.start().set(neckPos);
 			myDrum.start().add(translation);
 
 			myDrum.end().set(fredPos);
 			myDrum.end().add(translation);
 
 			// Mittelvector
 			myDrum.center().set(neckPos);
 			myDrum.center().add(translation2);
 		}
 	}
 
 	public void checkFredMatch(Player player) {
		PVector v1 = player.getHandRightAbsolute().get();
		PVector v2 = player.getHandLeftAbsolute().get();
 
 		PVector v3 = player.getHandLeftAbsolute().get();
 		PVector v4 = player.getHandRightAbsolute().get();
 
 		v1.normalize();
 		v2.normalize();
 
 		for (DrumSingle myDrum : _myDrums) {
 
 			// PVector rv = myDrum.rv(true);
 			PVector ov = myDrum.ov(true);
 
 			// Collision detection
			checkCollision(v2, v3, myDrum, true);
			checkCollision(v1, v4, myDrum, false);
 		}
 
 	}
 
 	private void checkCollision(PVector handAbsolute, PVector hand, DrumSingle myDrum, boolean left) {
 		PVector ov = myDrum.ov(true);
 
 		// Dot Product
 		float dotProduct = handAbsolute.dot(ov);
 
 		// Entfernung berechnen
 		float distance = hand.dist(myDrum.center());
 		float maxDistance = _myDrumSpace / 2;
 
 		if (distance <= maxDistance) {
 			// Crap
 			if (left) {
 				if (myDrum.dotProductLeft < 0 && dotProduct > 0) {
 
 					System.out.println("hit up " + myDrum.id);
 					midi.playMidi(myDrum.id, myDrum.id, true);
 
 				} else if (myDrum.dotProductLeft > 0 && dotProduct < 0) {
 
 					midi.playMidi(myDrum.id, myDrum.id, false);
 					System.out.println("hit down " + myDrum.id);
 				}
 			} else {
 				if (myDrum.dotProductRight < 0 && dotProduct > 0) {
 
 					System.out.println("hit up " + myDrum.id);
 					midi.playMidi(myDrum.id, myDrum.id, true);
 
 				} else if (myDrum.dotProductRight > 0 && dotProduct < 0) {
 
 					midi.playMidi(myDrum.id, myDrum.id, false);
 					System.out.println("hit down " + myDrum.id);
 				}
 			}
 
 		}
		// Neues Dot Product Speichern
 		if (left) {
 			myDrum.dotProductLeft = dotProduct;
 		} else {
 			myDrum.dotProductRight = dotProduct;
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
 		p.noStroke();
 		p.fill(255, 0, 255, 125);
 
 		for (DrumSingle myDrum : _myDrums) {
 
 			// Line Check
 			// p.line(myDrum.start().x, myDrum.start().y, myDrum.end().x,
 			// myDrum.end().y);
 
 			p.rect(myDrum.start().x, myDrum.start().y, _myDrumWidth, _myDrumHeight);
			p.ellipse(myDrum.center().x, myDrum.center().y, 10, 10);
 
 			// End and Start Vectors
 			// p.ellipse(myDrum.start().x, myDrum.start().y, 20, 20);
 			// p.ellipse(myDrum.end().x, myDrum.end().y, 20, 20);
 		}
 
 		p.fill(255);
 
 		// Draw Player Hands
		// p.ellipse(player.getHandLeftAbsolute().x,player.getHandLeftAbsolute().y,10,
		// 10);
		p.ellipse(player.getHandRightAbsolute().x, player.getHandRightAbsolute().y, 10, 10);
 	}
 }
