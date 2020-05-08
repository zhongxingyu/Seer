 package hu.silentsignal.android.nfcat;
 
 import android.app.Activity;
 import android.os.*;
 import android.content.*;
 import android.app.*;
 import android.nfc.*;
 import android.nfc.tech.MifareClassic;
 import android.widget.TextView;
 import java.net.*;
 import java.io.*;
 
 public class Main extends Activity
 {
     protected NfcAdapter mAdapter;
     protected IntentFilter[] mFilters;
     protected final static String[][] mTechLists = new String[][] { new String[] { MifareClassic.class.getName() } };
     protected PendingIntent mPendingIntent;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         mAdapter = NfcAdapter.getDefaultAdapter(this);
         mPendingIntent = PendingIntent.getActivity(this, 0,
                 new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
         final IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
 
         try {
             ndef.addDataType("*/*");
         } catch (IntentFilter.MalformedMimeTypeException e) {
             throw new RuntimeException("fail", e);
         }
         mFilters = new IntentFilter[] {
             ndef,
         };
 
         final Intent intent = getIntent();
         resolveIntent(intent);
     }
 
     @Override
     public void onResume() {
         super.onResume();
         mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
     }
 
     @Override
     public void onNewIntent(Intent intent) {
         resolveIntent(intent);
     }
 
     void resolveIntent(Intent intent) {
         final String action = intent.getAction();
 
         if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
             Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
             new NetworkService().execute(tagFromIntent);
         }
     }
 
     protected class NetworkService extends AsyncTask<Tag, String, Void> {
         protected PrintWriter output;
         protected MifareClassic mfc;
 
         @Override
         protected Void doInBackground(Tag... tags) {
             try {
                 mfc = MifareClassic.get(tags[0]);
                 mfc.connect();
                 final ServerSocket ss = new ServerSocket(0);
                 publishProgress(getString(R.string.listening, ss.getLocalPort()));
                 final Socket s = ss.accept();
                 publishProgress(getString(R.string.connected, s.getRemoteSocketAddress().toString()));
                 final BufferedReader input = new BufferedReader(
                         new InputStreamReader(s.getInputStream(), "UTF-8"));
                 output = new PrintWriter(
                         new OutputStreamWriter(s.getOutputStream(), "UTF-8"));
                 while (true) {
                     output.print("nfcat> ");
                     output.flush();
                     final String cmd = input.readLine();
                     if (cmd == null) break;
                     processCommand(cmd);
                 }
             } catch (IOException ioe) {
                 publishProgress(getString(R.string.exception, ioe.toString()));
             }
             return null;
         }
 
         protected void processCommand(final String cmd) throws IOException {
             try {
             if (cmd.equals("help")) {
                 output.println("Available commands are: rdbl, rdsc, wrbl, wrsc");
             } else if (cmd.indexOf("rdbl") == 0) {
                 final String[] params = cmd.split(" ");
                 if (params.length < 4) {
                    output.println("Usage: rdbl <sector> <A/B> <key>");
                 } else {
                     processReadBlock(params);
                 }
             } else {
                 output.println("Unrecognized command");
             }
             } catch (CommandException ne) {
                 output.println(getString(R.string.error, ne.getMessage()));
             }
         }
 
         protected void processReadBlock(final String[] params) throws IOException, CommandException {
             int blockIndex;
             try {
                 blockIndex = Integer.parseInt(params[1]);
             } catch (NumberFormatException nfe) {
                 throw new CommandException(getString(R.string.invalid_block_index, params[1]));
             }
             final int sectorIndex = mfc.blockToSector(blockIndex);
             authForSector(params[2], sectorIndex, params[3]);
             readAndSendBlockContents(blockIndex);
         }
 
         protected void authForSector(final String ab, final int sectorIndex,
                 final String hexkey) throws IOException, CommandException {
             final byte[] key = new byte[6];
             try {
                 hexStringToBytes(hexkey, key);
             } catch (Exception e) {
                 throw new CommandException(getString(R.string.invalid_key, hexkey));
             }
             boolean auth;
             if (ab.charAt(0) == 'A') {
                 auth = mfc.authenticateSectorWithKeyA(sectorIndex, key);
             } else {
                 auth = mfc.authenticateSectorWithKeyB(sectorIndex, key);
             }
             if (!auth) throw new CommandException(getString(R.string.auth_fail));
         }
 
         protected void readAndSendBlockContents(final int blockIndex) throws IOException {
             final byte[] contents = mfc.readBlock(blockIndex);
             output.print("Received: ");
             for (byte b : contents) {
                 output.format("%02X", b);
             }
             output.println("");
         }
 
         @Override
         protected void onProgressUpdate(String... progress) {
             super.onProgressUpdate(progress);
             final TextView tv = (TextView)findViewById(R.id.output);
             final StringBuilder sb = new StringBuilder(tv.getText());
             for (String s : progress) {
                 sb.append(s);
                 sb.append('\n');
             }
             tv.setText(sb);
         }
     }
 
     protected static void hexStringToBytes(final String input, final byte[] output) {
         final int len = output.length;
         for (int i = 0; i < len; i++) {
             output[i] = (byte)Integer.parseInt(input.substring(i * 2, i * 2 + 2), 16);
         }
     }
 
     protected static class CommandException extends Exception {
         public CommandException(String detailMessage) {
             super(detailMessage);
         }
     }
 
     @Override
     public void onPause() {
         super.onPause();
         mAdapter.disableForegroundDispatch(this);
     }
 }
