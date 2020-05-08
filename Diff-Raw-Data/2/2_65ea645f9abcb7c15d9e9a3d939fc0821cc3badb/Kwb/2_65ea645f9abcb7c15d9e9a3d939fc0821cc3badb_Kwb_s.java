 package commons.konoha2;
 
 import commons.sugar.KArray;
 
 public class Kwb {
 	KArray m;
 	int pos;
 	
 	public Kwb(KArray m) {
 		this.m = m;
 		this.pos = m.byteSize;
 	}
 
 	public void write(CTX ctx, String data, int byteLen) {
 		if(!(m.byteSize + byteLen < m.byteMax)) {
 			/*karray_expand(_ctx, m, m->bytesize + byteLen);*/
 		}
 		/*memcpy(m.bytebuf + m.byteSize, data, byteLen);*/
 		m.bytebuf += m.byteSize + data.substring(0,byteLen + 1);	
 		m.byteSize += byteLen;
 	}
 
 	public void putc(CTX ctx,int... ap) {
		String buf;
 		int len = 0;
 		for(int ch : ap) {
 			buf += ch;
 			len++;
 	 	}
 		write(ctx, buf, len);
 	}
 
 	public void vprintf(CTX ctx, Kwb wb, Object... ap) {//String... ap
 		//va_list ap2;
 		//va_copy(ap2, ap);
 		this.m = wb.m;//TODO this.m or KArray m = wb.m ?
 		Object ap2 = ap;
 		int s = m.byteSize;
 		int n = vsnprintf( m.bytebuf + s, m.byteMax - s, ap);//TODO
 		if(n >= (m.byteMax - s)) {
 			karray_expand(ctx, m, n + 1);//TODO
 			n = vsnprintf(m.bytebuf + s, m.byteMax - s, ap);
 		}
 		//va_end(ap2);
 		m.byteSize += n;
 	}
 
 	public void printf (CTX ctx, Kwb wb, Object... ap) {//TODO NO IDEA
 		//*note:fmt is an Array, the type is Sting. This means String[] fmt = new String[]
 		//va_list ap;
 		//va_start(ap, fmt);
 		vprintf(ctx, wb, ap);
 		//va_end(ap);
 	}
 
 	public String top(CTX ctx, Kwb wb, boolean ensureZero) {
 		KArray m = wb.m;
 		if(ensureZero) {
 			if( !(m.byteSize + 1 < m.byteMax) ) {
 				karray_expand(ctx, m, m.byteSize + 1);//TODO
 			}
 			m.bytebuf += 0;//TODO
 		}
 		return (m.bytebuf + wb.pos);
 	}
 
 	public void free(Kwb wb) {
 		this.m = wb.m;
 		bzero(m.bytebuf + wb.pos, m.byteSize - wb.pos);//TODO
 		this.m.byteSize = wb.pos;
 	}
 }
