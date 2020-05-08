 package session;
 
 import misc.Util;
 
 import java.io.*;
 
 public class SessionPattern
 {
 	/*
 	public static class TrackEntry
 	{
 		private SessionTrack trk; // track
 		private int pbeg, plen, poffs;
 		
 		public TrackEntry(SessionTrack trk, int pbeg, int plen, int poffs)
 		{
 			this.trk = trk;
 			this.pbeg = pbeg;
 			this.plen = plen;
 			this.poffs = poffs;
 		}
 	}
 	*/
 	
 	// format bollocks
 	
 	public static final int FORMAT_IT = 1;
 	public static final int FORMAT_MOD = 2;
 	public static final int FORMAT_S3M = 3;
 	public static final int FORMAT_XM = 4;
 	
 	// pattern info
 	
 	private int rows;
 	private SessionTrack[] tracks;
 	private int[] tidx;
 	
 	public SessionPattern(Session session, RandomAccessFile fp, int format) throws IOException
 	{
 		switch(format)
 		{
 			case FORMAT_IT:
 				loadDataIT(session, fp);
 				break;
 			case FORMAT_XM:
 			case FORMAT_MOD:
 			case FORMAT_S3M:
 				throw new RuntimeException("incorrect constructor for pattern format");
 			default:
 				throw new RuntimeException("pattern format not supported");
 		}
 	}
 	
 	public SessionPattern(Session session, RandomAccessFile fp, int format, int secondary) throws IOException
 	{
 		switch(format)
 		{
 			case FORMAT_MOD:
 				loadDataMOD(session, fp, secondary);
 				break;
 			case FORMAT_XM:
 				loadDataXM(session, fp, secondary);
 				break;
 			case FORMAT_IT:
 			case FORMAT_S3M:
 				throw new RuntimeException("incorrect constructor for pattern format");
 			default:
 				throw new RuntimeException("pattern format not supported");
 		}
 	}
 	
 	public SessionPattern(Session session, RandomAccessFile fp, int format, int[] secondary) throws IOException
 	{
 		switch(format)
 		{
 			case FORMAT_S3M:
 				loadDataS3M(session, fp, secondary);
 				break;
 			case FORMAT_IT:
 			case FORMAT_MOD:
 			case FORMAT_XM:
 				throw new RuntimeException("incorrect constructor for pattern format");
 			default:
 				throw new RuntimeException("pattern format not supported");
 		}
 	}
 	
 	private void loadDataXM(Session session, RandomAccessFile fp, int chnnum) throws IOException
 	{
 		// load header
 		InhibitedFileBlock ifb = new InhibitedFileBlock(fp, Integer.reverseBytes(fp.readInt())-4);
 		int packtype = ifb.read();
 		if(packtype != 0)
 			throw new RuntimeException(String.format("pattern packing %d not supported", packtype));
 		this.rows = 0xFFFF&(int)Short.reverseBytes(ifb.readShort());
 		int patsize = 0xFFFF&(int)Short.reverseBytes(ifb.readShort());
 		ifb.done();
 		
 		System.out.printf("pattern r=%d packsize=%d\n", this.rows, patsize);
 		ifb = new InhibitedFileBlock(fp, patsize);
 		
 		this.tracks = new SessionTrack[64];
 		
 		for(int c = 0; c < chnnum; c++)
 		{
 			tracks[c] = new SessionTrack(rows);
 		}
 		
 		int[] ld = new int[5];
 		
 		for(int r = 0; r < rows; r++)
 		{
 			for(int c = 0; c < chnnum; c++)
 			{
 				ld[0] = ld[1] = ld[2] = ld[3] = ld[4] = 0;
 				
 				int mask = ifb.read();
 				if((mask&0x80) != 0)
 				{
 					if((mask & 0x01) != 0)
 						ld[0] = ifb.read();
 					
 					if((mask & 0x02) != 0)
 						ld[1] = ifb.read();
 					
 					if((mask & 0x04) != 0)
 						ld[2] = ifb.read();
 					
 					if((mask & 0x08) != 0)
 						ld[3] = ifb.read();
 					
 					if((mask & 0x10) != 0)
 						ld[4] = ifb.read();
 				} else {
 					ld[0] = mask;
 					ld[1] = ifb.read();
 					ld[2] = ifb.read();
 					ld[3] = ifb.read();
 					ld[4] = ifb.read();
 				}
 				
 				// filter note
 				if(ld[0] >= 1 && ld[0] <= 96)
 					ld[0] = ld[0]-1 + 12;
 				else if(ld[0] == 97)
 					ld[0] = 255;
 				else
 					ld[0] = 253;
 				
 				// filter effects
 				// get the values
 				int vol = ld[2];
 				int eff = ld[3];
 				int efp = ld[4];
 				
 				// knock out some crap
 				
 				// check if we can transfer effects across
 				boolean voltrans = (
 					(vol >= 0x60 && vol <= 0xFF)
 						);
 				boolean efftrans = (
 					(eff >= 0x01 && eff <= 0x02 && (efp&3) == 0 && efp <= 0x09)
 					|| (eff == 0x03)
 					|| (eff == 0x04 && (efp & 0xF0) == 0)
 					|| eff == 0x0A
 					|| eff == 0x0C
 					|| (eff == 0x0E && efp >= 0xA0 && efp <= 0xBF)
 					|| (eff == 0x21 && efp >= 0x10 && efp <= 0x2F)
 						);
 				
 				// check if our effects are OK
 				boolean volok = (
 					(vol >= 0x10 && vol <= 0x50)
 					|| (vol >= 0x60 && vol <= 0x9F && (vol & 15) <= 0x09)
 					|| (vol >= 0xB0 && vol <= 0xBF && (vol & 15) <= 0x09)
 					|| (vol >= 0xF0 && vol <= 0xFF && (vol & 15) <= 0x09)
 						);
 				boolean effok = (
 					(eff >= 0x00 && eff <= 0x0B)
 					|| (eff >= 0x0D && eff <= 0x11)
 					|| eff == 0x19
 					|| eff == 0x1B
 					|| eff == 0x1D
 					|| (eff == 0x21 && efp >= 0x10 && efp <= 0x2F)
 						);
 				
 				// filter voleffects
 				if(vol >= 0x10 && vol <= 0x50)
 					ld[2] = vol-0x10;
 				else if(vol >= 0x60 && vol <= 0x6F) // vol slide down
 					ld[2] = (vol > 0x69 ? 0x69 : vol)-0x60+95;
 				else if(vol >= 0x70 && vol <= 0x7F) // vol slide up
 					ld[2] = (vol > 0x79 ? 0x79 : vol)-0x70+85;
 				else if(vol >= 0x80 && vol <= 0x8F) // fine vol slide down
 					ld[2] = (vol > 0x89 ? 0x89 : vol)-0x80+75;
 				else if(vol >= 0x90 && vol <= 0x9F) // fine vol slide up
 					ld[2] = (vol > 0x99 ? 0x99 : vol)-0x90+65;
 				else if(vol >= 0xB0 && vol <= 0xBF) // vibrato
 					ld[2] = (vol > 0xB9 ? 0xB9 : vol)-0xB0+65;
 				else if(vol >= 0xF0 && vol <= 0xFF) // porta
 				{
 					// TODO FILTER THIS CORRECTLY!!!
 					ld[2] = (vol > 0xF9 ? 0xF9 : vol)-0xF0+65;
 				} else 
 					ld[2] = 255;
 				
 				// filter effects
 				switch(eff)
 				{
 					case 0x00:
 						if(ld[4] != 0x00)
 							ld[3] = 0x0A;
 						break;
 					case 0x01:
 						if(ld[4] > 0xDF)
 							ld[4] = 0xDF;
 						ld[3] = 0x06;
 						break;
 					case 0x02:
 						if(ld[4] > 0xDF)
 							ld[4] = 0xDF;
 						ld[3] = 0x05;
 						break;
 					case 0x03:
 						ld[3] = 0x07;
 						break;
 					case 0x04:
 						ld[3] = 0x08;
 						break;
 					case 0x05:
 						ld[3] = 0x0C;
 						break;
 					case 0x06:
 						ld[3] = 0x0B;
 						break;
 					case 0x07:
 						ld[3] = 0x12;
 						break;
 					case 0x08:
 						ld[3] = 0x18;
 						break;
 					case 0x09:
 						ld[3] = 0x0F;
 						break;
 					case 0x0A:
 						// TODO see what happens when xy in Dxy are both nonzero
 						ld[3] = 0x04;
 						break;
 					case 0x0B:
 						ld[3] = 0x02;
 						break;
 					case 0x0C:
 						// TODO transfer this cleanly
 						if(ld[4] <= 0x40)
							ld[2] = 0x10 + ld[4];
 						ld[3] = 0;
 						ld[4] = 0;
 						break;
 					case 0x0D:
 						ld[3] = 0x03;
 						ld[4] = (ld[4]>>4)*10+ld[4];
 						break;
 					case 0x0E:
 						ld[3] = 0x13;
 						switch(ld[4]>>4)
 						{
 							case 0x1:
 								ld[3] = 0x06;
 								if(ld[4] == 0xA0)
 									ld[4] = 0x00;
 								else
 									ld[4] = 0xF0|(ld[4]&15);
 								break;
 							case 0x2:
 								ld[3] = 0x05;
 								if(ld[4] == 0xA0)
 									ld[4] = 0x00;
 								else
 									ld[4] = 0xF0|(ld[4]&15);
 								break;
 							case 0x3:
 								ld[4] = 0x10|(ld[4]&15);
 								break;
 							case 0x4:
 								ld[4] = 0x30|(ld[4]&15);
 								break;
 							case 0x5:
 								ld[4] = 0x20|(ld[4]&15);
 								break;
 							case 0x6:
 								ld[4] = 0xB0|(ld[4]&15);
 								break;
 							case 0x7:
 								ld[4] = 0x40|(ld[4]&15);
 								break;
 							case 0x8:
 								ld[4] = 0x80|(ld[4]&15);
 								break;
 							case 0x9:
 								ld[3] = 0x11;
 								ld[4] = (ld[4]&15);
 								break;
 							case 0xA:
 								if(ld[4] == 0xA0)
 									ld[4] = 0x00;
 								else
 									ld[4] = 0x0F|((ld[4]&15)<<4);
 								ld[3] = 0x04;
 								break;
 							case 0xB:
 								if(ld[4] == 0xA0)
 									ld[4] = 0x00;
 								else
 									ld[4] = 0xF0|(ld[4]&15);
 								ld[3] = 0x04;
 								break;
 							case 0xC:
 								ld[4] = 0xC0|(ld[4]&15);
 								break;
 							case 0xD:
 								ld[4] = 0xD0|(ld[4]&15);
 								break;
 							case 0xE:
 								ld[4] = 0xE0|(ld[4]&15);
 								break;
 							default:
 								ld[3] = 0x00;
 								break;
 						}
 						break;
 					case 0x0F:
 						if(ld[4] >= 0x20)
 							ld[3] = 0x14;
 						else
 							ld[3] = 0x01;
 						break;
 					case 0x10:
 						ld[3] = 0x16;
 						ld[4] *= 2;
 						break;
 					case 0x11:
 						ld[3] = 0x17;
 						break;
 					case 0x19:
 						ld[3] = 0x10;
 						break;
 					case 0x1B:
 						ld[3] = 0x11;
 						break;
 					case 0x1D:
 						if(ld[4] != 0)
 							ld[3] = 0x09;
 						else
 							ld[3] = 0x00;
 						break;
 					case 0x21:
 						if((ld[4]&0xF0) == 0x10)
 						{
 							ld[3] = 0x06;
 							ld[4] = 0xE0|(ld[4]&15);
 						} else if((ld[4]&0xF0) == 0x20) {
 							ld[3] = 0x05;
 							ld[4] = 0xE0|(ld[4]&15);
 						} else {
 							ld[3] = 0x00;
 							ld[4] = 0x00;
 						}
 						
 						if((ld[4]&15) == 0)
 							ld[4] = 0;
 						break;
 					default:
 						ld[3] = 0;
 						ld[4] = 0;
 						break;
 				}
 				
 				// do any transfers that we may need to do
 				// TODO!
 				/*
 				if((!volok) && efftrans)
 				{
 					switch(eff)
 					{
 					
 					}
 				}*/
 				
 				// set data
 				tracks[c].setData(r, ld);
 			}
 		}
 		
 		ifb.done();
 		
 		tidx = session.addTracks(tracks);
 	}
 	
 	private void loadDataS3M(Session session, RandomAccessFile fp, int[] chnmap) throws IOException
 	{
 		// TODO: do something interesting with the channel mapping
 		
 		int size = 0xFFFF&(int)Short.reverseBytes(fp.readShort());
 		this.tracks = new SessionTrack[64];
 		this.rows = 64;
 		
 		
 		int[] ld = new int[5];
 		
 		for(int r = 0; r < rows; r++)
 		{
 			while(true)
 			{
 				int mask = fp.read();
 				if(mask == 0)
 					break;
 				
 				int chn = (mask & 0x1F);
 				
 				if(tracks[chn] == null)
 					tracks[chn] = new SessionTrack(rows);
 				
 				ld[0] = 253;
 				ld[2] = 255;
 				ld[1] = ld[3] = ld[4] = 0;
 				
 				if((mask & 0x20) != 0)
 				{
 					int note = fp.read();
 					
 					if(note <= 0x8C)
 					{
 						note = (note>>4)*12+(note&15);
 						note += 12;
 					} else if(note != 254) {
 						note = 253;
 					}
 					
 					ld[0] = note;
 					ld[1] = fp.read();
 				}
 				
 				if((mask & 0x40) != 0)
 					ld[2] = fp.read();
 				
 				if((mask & 0x80) != 0)
 				{
 					ld[3] = fp.read();
 					ld[4] = fp.read();
 					
 					switch(ld[3])
 					{
 						case 0x03:
 							ld[4] = (ld[4]>>4)*10+ld[4];
 							break;
 						case 0x16:
 							ld[4] *= 2;
 							break;
 						case 0x18:
 							// ok apparently IT does this conversion
 							// so let's do the same damn thing
 							if(ld[4] == 0xA4)
 							{
 								ld[3] = 0x13;
 								ld[4] = 0x91;
 							} else {
 								ld[3] = ld[4] = 0x00;
 							}
 							break;
 						// some crap that isn't used in S3M
 						case 0x0D: // no CJA, .s3m doesn't support channel volumes --GM
 						case 0x0E:
 						case 0x10:
 						case 0x17:
 						case 0x19:
 						case 0x1A:
 							ld[3] = ld[4] = 0x00;
 							break;
 						
 					}
 				}
 				
 				tracks[chn].setData(r, ld);
 			}
 		}
 		
 		tidx = session.addTracks(tracks);
 	}
 	
 	private void loadDataMOD(Session session, RandomAccessFile fp, int chncount) throws IOException
 	{
 		this.tracks = new SessionTrack[64];
 		this.rows = 64;
 		
 		for(int c = 0; c < chncount; c++)
 		{
 			tracks[c] = new SessionTrack(rows);
 		}
 		
 		int[] ld = new int[5];
 		
 		for(int r = 0; r < 64; r++)
 		{
 			for(int c = 0; c < chncount; c++)
 			{
 				ld[0] = 253;
 				ld[2] = 255;
 				ld[1] = ld[3] = ld[4] = 0;
 				
 				int per = fp.readUnsignedShort();
 				int eff = fp.readUnsignedShort();
 				int ins = ((per>>8)&0xF0)|(eff>>12);
 				per &= 0xFFF;
 				eff &= 0xFFF;
 				int tent_eft = eff>>8;
 				int efp = eff&0xFF;
 				
 				if(per != 0)
 				{
 					// HA! HA! I'm using MATHEMATICS
 					int note = (int)(12.0f*Math.log(428.0f/((float)per))/Math.log(2.0)+60+0.5);
 					if(note < 0 || note >= 120)
 						note = 253;
 					ld[0] = note;
 				}
 				
 				int eft = 0;
 				
 				switch(tent_eft)
 				{
 					case 0x0:
 						if(efp != 0)
 							eft = 0x0A;
 						break;
 					case 0x1:
 						if(efp != 0)
 							eft = 0x06;
 						if(efp > 0xDF)
 							efp = 0xDF;
 						break;
 					case 0x2:
 						if(efp != 0)
 							eft = 0x05;
 						if(efp > 0xDF)
 							efp = 0xDF;
 						break;
 					case 0x3:
 						eft = 0x07;
 						break;
 					case 0x4:
 						eft = 0x08;
 						break;
 					case 0x5:
 						if(efp != 0)
 							eft = 0x0C;
 						else
 							eft = 0x07;
 						break;
 					case 0x6:
 						if(efp != 0)
 							eft = 0x0B;
 						else
 							eft = 0x08;
 						break;
 					case 0x7:
 						if(efp != 0)
 							eft = 0x12;
 						break;
 					case 0x8:
 						eft = 0x18;
 						break;
 					case 0x9:
 						// XXX: should this behave like PT1.x?
 						// (PT1.x adds instead of sets the offset.)
 						eft = 0x0F;
 						break;
 					case 0xA:
 						if(efp != 0)
 							eft = 0x04;
 						break;
 					case 0xB:
 						eft = 0x02;
 						break;
 					case 0xC:
 						if(efp <= 64)
 							ld[2] = efp;
 						efp = 0;
 						break;
 					case 0xD:
 						eft = 0x03;
 						// THIS IS STUPID
 						efp = (efp>>4)*10+(efp&15);
 						break;
 					case 0xE:
 						eft = 0x13;
 						switch(efp>>4)
 						{
 							case 0x0:
 								break;
 							case 0x1:
 								eft = 0x06;
 								efp = (efp&15)|0xF0;
 								break;
 							case 0x2:
 								eft = 0x05;
 								efp = (efp&15)|0xF0;
 								break;
 							case 0x3:
 								efp = (efp&15)|0x10;
 								break;
 							case 0x4:
 								efp = (efp&15)|0x30;
 								break;
 							case 0x5:
 								efp = (efp&15)|0x20;
 								break;
 							case 0x6:
 								efp = (efp&15)|0xB0;
 								break;
 							case 0x7:
 								efp = (efp&15)|0x40;
 								break;
 							case 0x8:
 								break;
 							case 0x9:
 								eft = 0x11;
 								efp = (efp&15);
 								break;
 							case 0xA:
 								eft = 0x04;
 								efp = ((efp&15)<<4)|0x0F;
 								break;
 							case 0xB:
 								eft = 0x04;
 								efp = (efp&15)|0xF0;
 								break;
 							case 0xC:
 								break;
 							case 0xD:
 								break;
 							case 0xE:
 								break;
 							case 0xF:
 								efp = 0;// funkrepeat not supported nor allocated!
 								break;
 							
 						}
 						break;
 					case 0xF:
 						if(efp < 0x20)
 							eft = 0x01;
 						else
 							eft = 0x14;
 						break;
 					
 				}
 				ld[1] = ins;
 				ld[3] = eft;
 				ld[4] = efp;
 				
 				tracks[c].setData(r, ld);
 			}
 		}
 		
 		tidx = session.addTracks(tracks);
 	}
 	
 	private void loadDataIT(Session session, RandomAccessFile fp) throws IOException
 	{
 		int size = 0xFFFF&(int)Short.reverseBytes(fp.readShort());
 		this.rows = 0xFFFF&(int)Short.reverseBytes(fp.readShort());
 		fp.readInt(); // skip 4 bytes
 		
 		this.tracks = new SessionTrack[64];
 		
 		int[] masks = new int[64];
 		int[][] ld = new int[64][];
 		
 		for(int i = 0; i < 64; i++)
 			ld[i] = new int[] {253, 0, 255, 0, 0};
 		
 		for(int r = 0; r < rows; r++)
 		{
 			while(true)
 			{
 				int csel = fp.read();
 				
 				if((csel&0x7F) == 0)
 					break;
 				
 				int chn = (csel & 0x7F)-1;
 				// TODO: check how 0x80 behaves, and chns > 64
 				
 				if(tracks[chn] == null)
 					tracks[chn] = new SessionTrack(rows);
 				
 				int mask = (csel & 0x80) != 0
 					? (masks[chn] = fp.read())
 					: masks[chn]
 				;
 				
 				if((mask & 0x01) != 0)
 					ld[chn][0] = fp.read();
 				if((mask & 0x02) != 0)
 					ld[chn][1] = fp.read();
 				if((mask & 0x04) != 0)
 					ld[chn][2] = fp.read();
 				if((mask & 0x08) != 0)
 				{
 					ld[chn][3] = fp.read();
 					ld[chn][4] = fp.read();
 				}
 				
 				if((mask & 0x11) != 0)
 					tracks[chn].setDataByte(r, 0, ld[chn][0]);
 				if((mask & 0x22) != 0)
 					tracks[chn].setDataByte(r, 1, ld[chn][1]);
 				if((mask & 0x44) != 0)
 					tracks[chn].setDataByte(r, 2, ld[chn][2]);
 				if((mask & 0x88) != 0)
 				{
 					tracks[chn].setDataByte(r, 3, ld[chn][3]);
 					tracks[chn].setDataByte(r, 4, ld[chn][4]);
 				}
 				
 			}
 		}
 		
 		tidx = session.addTracks(tracks);
 	}
 	
 	public SessionTrack getTrack(int idx)
 	{
 		return (idx >= 0 && idx < tracks.length
 			? tracks[idx]
 			: null);
 	}
 	
 	public int getRows()
 	{
 		return rows;
 	}
 }
