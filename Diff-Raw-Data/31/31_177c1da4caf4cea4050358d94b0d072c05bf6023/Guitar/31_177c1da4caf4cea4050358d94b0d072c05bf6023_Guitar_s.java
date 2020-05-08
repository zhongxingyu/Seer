 package com.android.shakemusic;
 
public class Guitar {
 
 	private int nHarm;
 	private int bpm;
	private final int fs = 44100;
	private final double inharmonity = 0.01;
	private final double gam = 1.7;
	private final double pi = Math.PI;
	private final double pisqr = pi * pi;

	Guitar(int nHarm, int bpm) {
 		this.nHarm = nHarm;
 		this.bpm = bpm;
 	}
 
	public short[] Note(int freq, double pluck_location) {

 		int Nt, jh, jhsqr;
 		double T, An, dfn;
		T = 60 / bpm;
 		Nt = (int) (fs * T);
 		short sj[] = new short[Nt];
 		// Make Note
 		int i, j;
 		for (i = 0; i < Nt; i++) {
 			sj[i] = 0;
 			for (j = 0; j < nHarm; j++) {
 				jh = j + 1;
 				jhsqr = jh * jh;
				An = 2
						/ ((pisqr) * jhsqr * pluck_location * (1 - pluck_location))
 						* Math.sin(jh * pi * pluck_location);
 				dfn = Math.sqrt(1 + jhsqr * inharmonity * inharmonity);
 				sj[i] = (short) (An * Math.exp(-gam * jh * i / fs) * Math.sin(2
 						* pi * i * freq * dfn * jh / fs));
 			}
 		}
		return null;
 	}
 }
