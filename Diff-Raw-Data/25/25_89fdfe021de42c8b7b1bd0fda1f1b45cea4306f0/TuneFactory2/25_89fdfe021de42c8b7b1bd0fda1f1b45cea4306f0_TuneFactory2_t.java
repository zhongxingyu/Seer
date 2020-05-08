 import org.soldomi.model.tune2.Tune;
 import org.soldomi.model.tune2.Syst;
 import org.soldomi.model.tune2.Staff;
 import org.soldomi.model.tune2.Sect;
 import org.soldomi.model.tune2.Block;
 import org.soldomi.model.tune2.Symbol;
 import org.soldomi.model.tune2.DurationSymbol;
import org.soldomi.model.tune2.SymbolType;
 import org.soldomi.model.tune2.NoteName;
import org.soldomi.model.tune2.NoteValue;
 import org.soldomi.model.tune2.Accidental;
 import org.soldomi.model.tune2.Pitch;
 import org.soldomi.model.tune2.Clef;
 
 import org.apache.commons.math3.fraction.Fraction;
 
 import java.util.List;
 import java.util.ArrayList;
 
 public interface TuneFactory2 {
     public Tune make();
 
     public static final TuneFactory2 brassBand = new TuneFactory2() {
 	    private List<Symbol> makeDoReMiFa(Long startTime) {
 		List<Symbol> symbols = new ArrayList<Symbol>();
 		symbols.add(Symbol.newClef(Fraction.ZERO, Clef.TREBLE));
		symbols.add(Symbol.newTimeSignature(Fraction.ZERO, 4, NoteValue.QUARTER));
 		symbols.add(Symbol.newNote(new Fraction(startTime), DurationSymbol.QUARTER, new Fraction(64), 0, new Pitch(NoteName.C, 3), Accidental.AUTO));
 		symbols.add(Symbol.newNote(new Fraction(startTime + 16), DurationSymbol.QUARTER, new Fraction(64), 0, new Pitch(NoteName.D, 3), Accidental.AUTO));
 		symbols.add(Symbol.newNote(new Fraction(startTime + 32), DurationSymbol.QUARTER, new Fraction(64), 0, new Pitch(NoteName.E, 3), Accidental.AUTO));
 		symbols.add(Symbol.newNote(new Fraction(startTime + 48), DurationSymbol.QUARTER, new Fraction(64), 0, new Pitch(NoteName.F, 3), Accidental.AUTO));
 		return symbols;
 	    }
 
 	    @Override public Tune make() {
 		System.out.println("TuneFactory2 brassBand - make");
 
 		Staff trumpet = new Staff("Trumpet");
 		Staff trombone = new Staff("Trombone");
 		Staff saxhorn = new Staff("Saxhorn");
 		Staff sousaphone = new Staff("Sousaphone");
 
 		List<Block> blocks = new ArrayList<Block>();
 		for (Long startTime : new Long[] {
 			0L,
 			64L,
 			128L
 		    }) {
 
 		    List<Symbol> trumpetSymbols = makeDoReMiFa(startTime);
 		    List<Symbol> tromboneSymbols = makeDoReMiFa(startTime);
 		    List<Symbol> saxhornSymbols = makeDoReMiFa(startTime);
 		    List<Symbol> sousaphoneSymbols = makeDoReMiFa(startTime);
 
 		    trumpet = trumpet.addSymbols(trumpetSymbols);
 		    trombone = trombone.addSymbols(tromboneSymbols);
 		    saxhorn = saxhorn.addSymbols(saxhornSymbols);
 		    sousaphone = sousaphone.addSymbols(sousaphoneSymbols);
 
 		    blocks.add(new Block(startTime)
 			       .addSymbols(trumpetSymbols)
 			       .addSymbols(tromboneSymbols)
 			       .addSymbols(saxhornSymbols)
 			       .addSymbols(sousaphoneSymbols)
 			       );
 		}
 
 
 		Tune tune = new Tune(null, "New brass band tune");
 		for (Staff staff : new Staff[] {
 			trumpet,
 			trombone,
 			saxhorn,
 			sousaphone
 		    }) {
 		    tune = tune.addSyst(new Syst(staff.name)
 					.addStaff(staff));
 		}
 
 		Sect sect = new Sect(0L);
 		for (Block block : blocks) {
 		    sect = sect.addBlock(block);
 		}
 		tune = tune.addSect(sect);
 
 		System.out.println(Test.printTuneInfo(tune));
 		return tune;
 	    }
 
 	};
 
 }
